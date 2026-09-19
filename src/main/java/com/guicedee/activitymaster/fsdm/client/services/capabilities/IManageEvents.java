package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEventType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

/**
 * Interface for managing events.
 * This interface provides methods for adding, updating, and querying events.
 *
 * @param <J> The type of the entity that implements this interface
 */
public interface IManageEvents<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>> {
    private String getEventRelationshipTable() {
        String className = getClass().getCanonicalName() + "XEvent";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>> getEventRelationshipClass() {
        String joinTableName = getEventRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find eventItemType linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures an event link value.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    void configureEventLinkValue(IWarehouseRelationshipTable linkTable, J primary, IEvent<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins of the read + create family (add / addOrReuse /
    // addOrUpdate). Reads verbatim; writes use session.insert + system.getEnterprise() + the stateless
    // resolveDefaultGroupFolderTokens/createDefaultSecurity path. The unused findEventType lookup is
    // skipped (its result was discarded in the managed flow). The pure-close mutations (expire / archive /
    // remove) are twinned below via a stateless full-row session.update (the managed merge/detach path and
    // the SCD-versioning update() are not stateless-portable: createMutationQuery(HQL) is blocked on a
    // stateless session, and merge requires a persistence context).
    // =============================================================================================

    /** Stateless variant of {@link #expireEvents(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> expireEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventStateless(session, eventTypeName, classificationName, searchValue, value, 0, system, identityToken);
    }

    /** Stateless variant of {@link #archiveEvents(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> archiveEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventStateless(session, eventTypeName, classificationName, searchValue, value, 1, system, identityToken);
    }

    /** Stateless variant of {@link #removeEvents(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> removeEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventStateless(session, eventTypeName, classificationName, searchValue, value, 2, system, identityToken);
    }

    /**
     * Shared stateless SCD-close for the event relationship link. {@code mode}: 0=expire (effective-to only),
     * 1=archive (archived flag), 2=remove (deleted flag). Mirrors the managed semantics exactly: no-op when the
     * link is absent or its current value already equals {@code value}; otherwise closes the active row via a
     * full-row {@code session.update} (no bulk HQL — blocked on a stateless session).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Uni<Void> closeEventStateless(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, int mode, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;

        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .chain(eventItemType -> tableForClassification.builder(session)
                        .findLink((J) this, null, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .map(r -> (Object) r)
                        .onFailure(NoResultException.class)
                        .recoverWithItem((Object) null)
                        .chain(resultObj -> {
                            IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existing =
                                    (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) resultObj;
                            if (existing == null || Strings.nullToEmpty(value).equals(existing.getValue())) {
                                return Uni.createFrom().voidItem();
                            }
                            existing.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                            if (mode == 0) {
                                return session.update(existing).replaceWithVoid();
                            }
                            Uni<? extends com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag<?, ?>> flagUni =
                                    (mode == 1) ? flagService.getArchivedFlag(session, enterprise, identityToken)
                                                : flagService.getDeletedFlag(session, enterprise, identityToken);
                            return flagUni.chain(flag -> {
                                existing.setActiveFlagID(flag);
                                return session.update(existing).replaceWithVoid();
                            });
                        }));
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> findEvent(Mutiny.StatelessSession session, IEventType<?, ?> eventType, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        return tableForClassification.builder(session)
                .findLink((J) this, null, value)
                .inActiveRange()
                .inDateRange()
                .canRead(system, identityToken)
                .get()
                .map(result -> {
                    if (result == null) { throw new NoSuchElementException("Event not found"); }
                    return (IRelationshipValue<J, IEvent<?, ?>, ?>) result;
                });
    }

    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IEvent<?, ?>, ?>>> findEventsAll(Mutiny.StatelessSession session, IEventType<?, ?> eventType, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        return tableForClassification.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .inDateRange()
                .canRead(system, identityToken)
                .getAll()
                .map(list -> (List<IRelationshipValue<J, IEvent<?, ?>, ?>>) list);
    }

    default Uni<Boolean> hasEvents(Mutiny.StatelessSession session, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfEvents(session, eventItemTypeName, system, identityToken).map(count -> count > 0);
    }

    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfEvents(Mutiny.StatelessSession session, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        return tableForClassification.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .inDateRange()
                .canRead(system, identityToken)
                .getCount();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addEvents(Mutiny.StatelessSession session, String eventType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return classificationService.find(session, finalClassificationName, system, identityToken)
                .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                        .chain(activeFlag -> {
                            tableForClassification.setEnterpriseID(enterprise);
                            tableForClassification.setValue(value);
                            tableForClassification.setSystemID(system);
                            tableForClassification.setClassificationID(classification);
                            tableForClassification.setOriginalSourceSystemID(system.getId());
                            tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                            tableForClassification.setActiveFlagID(activeFlag);
                            configureEventLinkValue(tableForClassification, (J) this, null, classification, value, enterprise);
                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                            if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                            return session.insert(tableForClassification)
                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                            .onFailure().recoverWithItem(0L))
                                    .replaceWith((IRelationshipValue<J, IEvent<?, ?>, ?>) tableForClassification);
                        }));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addOrReuseEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        return tableForClassification.builder(session)
                .findLink((J) this, null, searchValue)
                .inActiveRange()
                .withClassification(finalClassificationName, system)
                .inDateRange()
                .get()
                .onFailure(NoResultException.class)
                .recoverWithUni(() -> (Uni) addEvents(session, eventTypeName, value, finalClassificationName, system, identityToken))
                .chain(result -> Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) result));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addOrUpdateEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return classificationService.find(session, finalClassificationName, system, identityToken)
                .chain(classification -> tableForClassification.builder(session)
                        .findLink((J) this, null, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .get()
                        .onFailure(NoResultException.class)
                        .recoverWithUni(() -> (Uni) addEvents(session, eventTypeName, value, finalClassificationName, system, identityToken))
                        .chain(result -> {
                            IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existingTable =
                                    (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) result;
                            if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable);
                            }
                            IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                            ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                            return flagService.getArchivedFlag(session, enterprise, identityToken)
                                    .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                    .chain(() -> {
                                        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> newTableForClassification = get(getEventRelationshipClass());
                                        newTableForClassification.setId(null);
                                        newTableForClassification.setSystemID(system);
                                        newTableForClassification.setOriginalSourceSystemID(system.getId());
                                        newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
                                        newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                        newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                        newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                        newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                        return flagService.getActiveFlag(session, enterprise, identityToken)
                                                .chain(activeFlag -> {
                                                    newTableForClassification.setActiveFlagID(activeFlag);
                                                    newTableForClassification.setValue(value);
                                                    newTableForClassification.setEnterpriseID(enterprise);
                                                    configureEventLinkValue(newTableForClassification, (J) this, null, classification, value, enterprise);
                                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                    return session.insert(newTableForClassification)
                                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                    .onFailure().recoverWithItem(0L))
                                                            .replaceWith((IRelationshipValue<J, IEvent<?, ?>, ?>) newTableForClassification);
                                                });
                                    });
                        }));
    }

    /**
     * Stateless variant of {@link #updateEvents(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}
     * — SCD retire+reinsert only when the link already exists (no-op if absent).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Void> updateEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return classificationService.find(session, finalClassificationName, system, identityToken)
                .chain(classification -> get(getEventRelationshipClass()).builder(session)
                        .findLink((J) this, null, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .map(r -> (Object) r)
                        .onFailure(NoResultException.class)
                        .recoverWithItem((Object) null)
                        .chain(resultObj -> {
                            IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existing =
                                    (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) resultObj;
                            if (existing == null || Strings.nullToEmpty(value).equals(existing.getValue())) {
                                return Uni.createFrom().voidItem();
                            }
                            return flagService.getArchivedFlag(session, enterprise, identityToken)
                                    .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existing, existing.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                    .chain(() -> {
                                        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> newRow = get(getEventRelationshipClass());
                                        newRow.setId(null);
                                        newRow.setSystemID(system);
                                        newRow.setOriginalSourceSystemID(system.getId());
                                        newRow.setOriginalSourceSystemUniqueID(existing.getId());
                                        newRow.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                        newRow.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                        newRow.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                        newRow.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                        return flagService.getActiveFlag(session, enterprise, identityToken).chain(activeFlag -> {
                                            newRow.setActiveFlagID(activeFlag);
                                            newRow.setValue(value);
                                            newRow.setEnterpriseID(enterprise);
                                            configureEventLinkValue(newRow, (J) this, null, classification, value, enterprise);
                                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newRow;
                                            return session.insert(newRow)
                                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                            .onFailure().recoverWithItem(0L))
                                                    .replaceWithVoid();
                                        });
                                    });
                        }));
    }
}

