package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEventType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"unused", "DuplicatedCode"})
public interface IManageEventTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>> {
    private String getEventTypeRelationshipTable() {
        String className = getClass().getCanonicalName() + "XEventType";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>> getEventTypeRelationshipClass() {
        String joinTableName = getEventTypeRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find eventItemType linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures an event type link value.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    @SuppressWarnings("rawtypes")
    void configureEventTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IEventType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins of the read + create family. Secondary IEventType is
    // resolved via the stateless IEventService.findEventType; writes use session.insert +
    // system.getEnterprise() + the stateless default-security path. configureEventTypeLinkValue is
    // session-free. update / expire / archive / remove (session.merge based) are not twinned here.
    // =============================================================================================

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> findEventType(Mutiny.StatelessSession session, String classificationName, String eventType, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        return eventService.findEventType(session, eventType, system, identityToken)
                .chain(eventItemType -> tableForClassification.builder(session)
                        .findLink((J) this, eventItemType, value)
                        .inActiveRange()
                        .withClassification(classificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .onItem().ifNull().failWith(() -> new NoSuchElementException("Event type not found"))
                        .map(item -> (IRelationshipValue<J, IEventType<?, ?>, ?>) item));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<List<IRelationshipValue<J, IEventType<?, ?>, ?>>> findEventTypesAll(Mutiny.StatelessSession session, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
        return tableForClassification.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .withClassification(classificationName, system)
                .inDateRange()
                .canRead(system, identityToken)
                .getAll()
                .map(list -> (List<IRelationshipValue<J, IEventType<?, ?>, ?>>) list);
    }

    default Uni<Boolean> hasEventTypes(Mutiny.StatelessSession session, String classificationName, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfEventTypes(session, classificationName, eventItemTypeName, system, identityToken).map(count -> count > 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Long> numberOfEventTypes(Mutiny.StatelessSession session, String classificationName, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        return eventService.findEventType(session, eventItemTypeName, system, identityToken)
                .chain(eventType -> tableForClassification.builder(session)
                        .findLink((J) this, eventType, null)
                        .inActiveRange()
                        .withClassification(classificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .getCount());
    }

    default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addEventTypes(Mutiny.StatelessSession session, Enum<?> eventType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        return addEventTypes(session, eventType.toString(), value, classificationName, system, identityToken);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addEventTypes(Mutiny.StatelessSession session, String eventType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return eventService.findEventType(session, eventType, system, identityToken)
                .chain(eventItemType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                                .chain(activeFlag -> {
                                    tableForClassification.setEnterpriseID(enterprise);
                                    tableForClassification.setValue(value);
                                    tableForClassification.setSystemID(system);
                                    tableForClassification.setOriginalSourceSystemID(system.getId());
                                    tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                    tableForClassification.setActiveFlagID(activeFlag);
                                    configureEventTypeLinkValue(tableForClassification, (J) this, eventItemType, classification, value, enterprise);
                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                    if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                                    return session.insert(tableForClassification)
                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                    .onFailure().recoverWithItem(0L))
                                            .replaceWith((IRelationshipValue<J, IEventType<?, ?>, ?>) tableForClassification);
                                })));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addOrReuseEventTypes(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .onItem().transformToUni(eventItemType -> tableForClassification.builder(session)
                        .findLink((J) this, eventItemType, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .onFailure(NoResultException.class)
                        .recoverWithUni(() -> (Uni) addEventTypes(session, eventTypeName, value, finalClassificationName, system, identityToken))
                        .chain(result -> Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) result)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addOrUpdateEventTypes(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .chain(eventItemType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> tableForClassification.builder(session)
                                .findLink((J) this, eventItemType, searchValue)
                                .inActiveRange()
                                .withClassification(finalClassificationName, system)
                                .inDateRange()
                                .canRead(system, identityToken)
                                .get()
                                .onFailure(NoResultException.class)
                                .recoverWithUni(() -> (Uni) addEventTypes(session, eventTypeName, value, finalClassificationName, system, identityToken))
                                .chain(result -> {
                                    IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existingTable =
                                            (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) result;
                                    if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                        return Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) existingTable);
                                    }
                                    IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                    ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                                    return flagService.getArchivedFlag(session, enterprise, identityToken)
                                            .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                            .chain(() -> {
                                                IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getEventTypeRelationshipClass());
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
                                                            configureEventTypeLinkValue(newTableForClassification, (J) this, eventItemType, classification, value, enterprise);
                                                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                            return session.insert(newTableForClassification)
                                                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                            .onFailure().recoverWithItem(0L))
                                                                    .replaceWith((IRelationshipValue<J, IEventType<?, ?>, ?>) newTableForClassification);
                                                        });
                                            });
                                })));
    }

    // ---- Stateless SCD close mutations (expire / archive / remove) via full-row session.update ----

    /** Stateless variant of {@link #expireEventTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> expireEventTypes(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventTypesStateless(session, eventTypeName, classificationName, searchValue, value, 0, system, identityToken);
    }

    /** Stateless variant of {@link #archiveEventTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> archiveEventTypes(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventTypesStateless(session, eventTypeName, classificationName, searchValue, value, 1, system, identityToken);
    }

    /** Stateless variant of {@link #removeEventTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> removeEventTypes(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventTypesStateless(session, eventTypeName, classificationName, searchValue, value, 2, system, identityToken);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Uni<Void> closeEventTypesStateless(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, int mode, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;

        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .chain(eventItemType -> tableForClassification.builder(session)
                        .findLink((J) this, eventItemType, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .map(r -> (Object) r)
                        .onFailure(NoResultException.class)
                        .recoverWithItem((Object) null)
                        .chain(resultObj -> {
                            IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existing =
                                    (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) resultObj;
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

    /**
     * Stateless variant of {@link #updateEventTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}
     * — SCD retire+reinsert only when the link already exists (no-op if absent).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Void> updateEventTypes(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IEventService<?> eventService = get(IEventService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .chain(eventItemType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> get(getEventTypeRelationshipClass()).builder(session)
                                .findLink((J) this, eventItemType, searchValue)
                                .inActiveRange()
                                .withClassification(finalClassificationName, system)
                                .inDateRange()
                                .canRead(system, identityToken)
                                .get()
                                .map(r -> (Object) r)
                                .onFailure(NoResultException.class)
                                .recoverWithItem((Object) null)
                                .chain(resultObj -> {
                                    IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existing =
                                            (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) resultObj;
                                    if (existing == null || Strings.nullToEmpty(value).equals(existing.getValue())) {
                                        return Uni.createFrom().voidItem();
                                    }
                                    return flagService.getArchivedFlag(session, enterprise, identityToken)
                                            .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existing, existing.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                            .chain(() -> {
                                                IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> newRow = get(getEventTypeRelationshipClass());
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
                                                    configureEventTypeLinkValue(newRow, (J) this, eventItemType, classification, value, enterprise);
                                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newRow;
                                                    return session.insert(newRow)
                                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                    .onFailure().recoverWithItem(0L))
                                                            .replaceWithVoid();
                                                });
                                            });
                                })));
    }
}

