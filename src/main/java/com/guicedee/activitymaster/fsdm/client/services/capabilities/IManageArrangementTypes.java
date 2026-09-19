package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService;
import com.guicedee.activitymaster.fsdm.client.services.IArrangementsService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangementType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.NoResultException;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.get;

@SuppressWarnings({"DuplicatedCode", "unused"})
public interface IManageArrangementTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>> {
    private String getArrangementTypesRelationshipTable() {
        String className = getClass().getCanonicalName() + "XArrangementType";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?>> getArrangementTypeRelationshipClass() {
        String joinTableName = getArrangementTypesRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find arrangementType linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures an arrangement type.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    @SuppressWarnings("rawtypes")
    void configureArrangementTypeAddable(IWarehouseRelationshipTable linkTable, J primary, IArrangementType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, ISystems<?, ?> system);

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins. Reads resolve the secondary IArrangementType via the
    // stateless IArrangementsService.findArrangementType; object-based writes use session.insert +
    // system.getEnterprise() + the stateless default-security path. configureArrangementTypeAddable is
    // session-free.
    // =============================================================================================

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> findArrangementType(Mutiny.StatelessSession session, String classification, String arrangementTypeName, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementTypeRelationshipClass());
        IArrangementsService<?> partyService = get(IArrangementsService.class);
        return partyService.findArrangementType(session, arrangementTypeName, system, identityToken)
                .chain(arrangementType -> {
                    IQueryBuilderRelationships<?, ?, J, IArrangementType<?, ?>, java.util.UUID> q
                            = relationshipTable.builder(session)
                            .findLink((J) this, arrangementType, null)
                            .inActiveRange()
                            .withClassification(classification, system)
                            .withValue(searchValue)
                            .inDateRange()
                            .withEnterprise(system.getEnterprise())
                            .canRead(system, identityToken);
                    if (first) { q.setMaxResults(1); }
                    if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
                    return q.get()
                            .onItem().ifNull().failWith(() -> new NoSuchElementException("Arrangement type not found"))
                            .map(item -> (IRelationshipValue<J, IArrangementType<?, ?>, ?>) item);
                });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<List<IRelationshipValue<J, IArrangementType<?, ?>, ?>>> findArrangementTypesAll(Mutiny.StatelessSession session, String classification, String arrangementTypeName, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementTypeRelationshipClass());
        IArrangementsService<?> partyService = get(IArrangementsService.class);
        return partyService.findArrangementType(session, arrangementTypeName, system, identityToken)
                .chain(arrangementType -> {
                    IQueryBuilderRelationships<?, ?, J, IArrangementType<?, ?>, java.util.UUID> q
                            = relationshipTable.builder(session)
                            .findLink((J) this, arrangementType, null)
                            .inActiveRange()
                            .withClassification(classification, system)
                            .withValue(searchValue)
                            .inDateRange()
                            .withEnterprise(system.getEnterprise())
                            .canRead(system, identityToken);
                    if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
                    return q.getAll().map(list -> (List<IRelationshipValue<J, IArrangementType<?, ?>, ?>>) list);
                });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Long> numberOfArrangementTypes(Mutiny.StatelessSession session, String classificationValue, String arrangementTypeName, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementTypeRelationshipClass());
        final String finalClassificationValue = classificationValue == null ? DefaultClassifications.NoClassification.classificationValue() : classificationValue;
        IArrangementsService<?> partyService = get(IArrangementsService.class);
        return partyService.findArrangementType(session, arrangementTypeName, system, identityToken)
                .chain(arrangementType -> relationshipTable.builder(session)
                        .findLink((J) this, null, value)
                        .withClassification(finalClassificationValue, system)
                        .inActiveRange()
                        .inDateRange()
                        .canRead(system, identityToken)
                        .getCount());
    }

    default Uni<Boolean> hasArrangementTypes(Mutiny.StatelessSession session, String classificationName, String arrangementTypeTypeName, String searchValue, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfArrangementTypes(session, classificationName, arrangementTypeTypeName, searchValue, system, identityToken).map(count -> count > 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> addArrangementType(Mutiny.StatelessSession session, IArrangementType<?, ?> arrangementType, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementTypeRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final var enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagService = get(IActiveFlagService.class);
        com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> sts = get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> activeFlagService.getActiveFlag(session, enterprise, identityToken)
                        .chain(activeFlag -> {
                            tableForClassification.setEnterpriseID(enterprise);
                            tableForClassification.setValue(Strings.nullToEmpty(value));
                            tableForClassification.setSystemID(system);
                            tableForClassification.setOriginalSourceSystemID(system.getId());
                            tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                            tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                            tableForClassification.setActiveFlagID(activeFlag);
                            tableForClassification.setClassificationID(classification);
                            configureArrangementTypeAddable(tableForClassification, (J) this, arrangementType, classification, value, system);
                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                            if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                            return session.insert(tableForClassification)
                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                            .onFailure().recoverWithItem(0L))
                                    .replaceWith((IRelationshipValue<J, IArrangementType<?, ?>, ?>) tableForClassification);
                        }));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> addOrReuseArrangementType(Mutiny.StatelessSession session, String classificationValue, IArrangementType<?, ?> arrangementTypeType, String searchValue, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementTypeRelationshipClass());
        return tableForClassification.builder(session)
                .findLink((J) this, arrangementTypeType, null)
                .withValue(searchValue)
                .inActiveRange()
                .inDateRange()
                .withClassification(classificationValue, system)
                .get()
                .onFailure(NoResultException.class)
                .recoverWithUni(() -> (Uni) addArrangementType(session, arrangementTypeType, classificationValue, searchValue, system, identityToken))
                .chain(result -> Uni.createFrom().item((IRelationshipValue<J, IArrangementType<?, ?>, ?>) result));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> addOrUpdateArrangementType(Mutiny.StatelessSession session, String classificationValue, IArrangementType<?, ?> arrangementTypeType, String searchValue, String storeValue, ISystems<?, ?> system, UUID... identityToken) {
        final IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementTypeRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final var enterprise = system.getEnterprise();
        return classificationService.find(session, classificationValue, system, identityToken)
                .chain(classification -> tableForClassification.builder(session)
                        .findLink((J) this, arrangementTypeType, null)
                        .withValue(searchValue)
                        .inActiveRange()
                        .inDateRange()
                        .withClassification(classificationValue, system)
                        .get()
                        .onFailure(NoResultException.class)
                        .recoverWithUni(() -> (Uni) addArrangementType(session, arrangementTypeType, classificationValue, storeValue, system, identityToken))
                        .chain(result -> {
                            IRelationshipValue<J, IArrangementType<?, ?>, ?> existingRelation = (IRelationshipValue<J, IArrangementType<?, ?>, ?>) result;
                            if (Strings.nullToEmpty(storeValue).equals(existingRelation.getValue())) {
                                return Uni.createFrom().item(existingRelation);
                            }
                            final IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?>) result;
                            IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                            com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> sts = get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
                            return flagService.getArchivedFlag(session, enterprise, identityToken)
                                    .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                    .chain(() -> {
                                        IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getArrangementTypeRelationshipClass());
                                        newTableForClassification.setId(null);
                                        newTableForClassification.setClassificationID(existingTable.getClassificationID());
                                        newTableForClassification.setSystemID(system);
                                        newTableForClassification.setOriginalSourceSystemID(existingTable.getId());
                                        newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
                                        newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                        newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                        newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                        newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                        return flagService.getActiveFlag(session, enterprise, identityToken)
                                                .chain(activeFlag -> {
                                                    newTableForClassification.setActiveFlagID(activeFlag);
                                                    newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                    newTableForClassification.setEnterpriseID(enterprise);
                                                    configureArrangementTypeAddable(newTableForClassification, existingTable.getPrimary(), existingTable.getSecondary(), classification, storeValue, system);
                                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                    return session.insert(newTableForClassification)
                                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                    .onFailure().recoverWithItem(0L))
                                                            .replaceWith((IRelationshipValue<J, IArrangementType<?, ?>, ?>) newTableForClassification);
                                                });
                                    });
                        }));
    }
}
