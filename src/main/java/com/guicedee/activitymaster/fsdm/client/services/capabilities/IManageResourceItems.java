package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"DuplicatedCode", "unused"})
public interface IManageResourceItems<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>> {
    /**
     * Gets the Vertx instance for executing blocking operations.
     * This method should be implemented by classes that implement this interface.
     *
     * @return The Vertx instance
     */
    default Vertx getVertx() {
        return get(Vertx.class);
    }

    private String getResourceItemsRelationshipTable() {
        String className = getClass().getCanonicalName() + "XResourceItem";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?>> getResourceItemRelationshipClass() {
        String joinTableName = getResourceItemsRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find resourceItem linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures a resource item link value.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    @SuppressWarnings("rawtypes")
    void configureResourceItemAddable(IWarehouseRelationshipTable linkTable, J primary, IResourceItem<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins. Reads verbatim; writes use session.insert +
    // system.getEnterprise() + the stateless resolveDefaultGroupFolderTokens/createDefaultSecurity path.
    // =============================================================================================

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> findResourceItem(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> relationshipTable = get(getResourceItemRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IResourceItem<?, ?>, java.util.UUID> q
                = relationshipTable.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .withClassification(classification, system)
                .withValue(searchValue)
                .inDateRange()
                .withEnterprise(system.getEnterprise())
                .canRead(system, identityToken);
        if (first) { q.setMaxResults(1); }
        if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
        return q.get()
                .onItem().ifNull().failWith(() -> new NoSuchElementException("Resource item not found"))
                .map(item -> (IRelationshipValue<J, IResourceItem<?, ?>, ?>) item);
    }

    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IResourceItem<?, ?>, ?>>> findResourceItemsAll(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> relationshipTable = get(getResourceItemRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IResourceItem<?, ?>, java.util.UUID> q
                = relationshipTable.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .withClassification(classification, system)
                .withValue(searchValue)
                .inDateRange()
                .withEnterprise(system.getEnterprise())
                .canRead(system, identityToken);
        if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
        return q.getAll().map(list -> (List<IRelationshipValue<J, IResourceItem<?, ?>, ?>>) list);
    }

    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfResourceItems(Mutiny.StatelessSession session, String classificationValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> relationshipTable = get(getResourceItemRelationshipClass());
        final String finalClassificationValue = classificationValue == null ? DefaultClassifications.NoClassification.classificationValue() : classificationValue;
        return relationshipTable.builder(session)
                .findLink((J) this, null, value)
                .withClassification(finalClassificationValue, system)
                .inActiveRange()
                .inDateRange()
                .canRead(system, identityToken)
                .getCount();
    }

    default Uni<Boolean> hasResourceItems(Mutiny.StatelessSession session, String resourceItemName, String searchValue, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfResourceItems(session, resourceItemName, searchValue, system, identityToken).map(count -> count > 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> addResourceItem(Mutiny.StatelessSession session, String classificationName, IResourceItem<?, ?> resourceItem, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return classificationService.find(session, finalClassificationName, system, identityToken)
                .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                        .chain(activeFlag -> {
                            tableForClassification.setEnterpriseID(enterprise);
                            tableForClassification.setValue(Strings.nullToEmpty(value));
                            tableForClassification.setSystemID(system);
                            tableForClassification.setOriginalSourceSystemID(system.getId());
                            tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                            tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                            tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                            tableForClassification.setActiveFlagID(activeFlag);
                            tableForClassification.setClassificationID(classification);
                            configureResourceItemAddable(tableForClassification, (J) this, resourceItem, classification, value, enterprise);
                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                            if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                            return session.insert(tableForClassification)
                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                            .onFailure().recoverWithItem(0L))
                                    .replaceWith((IRelationshipValue<J, IResourceItem<?, ?>, ?>) tableForClassification);
                        }));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> addOrReuseResourceItem(Mutiny.StatelessSession session, String classificationValue, IResourceItem<?, ?> resourceItem, String searchValue, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemRelationshipClass());
        final String finalClassificationName = Strings.isNullOrEmpty(classificationValue) ? DefaultClassifications.NoClassification.toString() : classificationValue;
        return tableForClassification.builder(session)
                .findLink((J) this, null, searchValue)
                .inActiveRange()
                .withClassification(finalClassificationName, system)
                .inDateRange()
                .get()
                .onFailure(NoResultException.class)
                .recoverWithUni(() -> (Uni) addResourceItem(session, finalClassificationName, resourceItem, searchValue, system, identityToken))
                .chain(result -> Uni.createFrom().item((IRelationshipValue<J, IResourceItem<?, ?>, ?>) result));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> addOrUpdateResourceItem(Mutiny.StatelessSession session, String classificationValue, IResourceItem<?, ?> resourceItem, String searchValue, String storeValue, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationValue) ? DefaultClassifications.NoClassification.toString() : classificationValue;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return classificationService.find(session, finalClassificationName, system, identityToken)
                .chain(classification -> tableForClassification.builder(session)
                        .findLink((J) this, null, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .get()
                        .onFailure(NoResultException.class)
                        .recoverWithUni(() -> (Uni) addResourceItem(session, finalClassificationName, resourceItem, storeValue, system, identityToken))
                        .chain(result -> {
                            IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> existingTable =
                                    (IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?>) result;
                            if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
                                return Uni.createFrom().item((IRelationshipValue<J, IResourceItem<?, ?>, ?>) existingTable);
                            }
                            IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                            ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                            return flagService.getArchivedFlag(session, enterprise, identityToken)
                                    .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                    .chain(() -> {
                                        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> newTableForClassification = get(getResourceItemRelationshipClass());
                                        newTableForClassification.setId(null);
                                        newTableForClassification.setClassificationID(existingTable.getClassificationID());
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
                                                    newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                    newTableForClassification.setEnterpriseID(enterprise);
                                                    configureResourceItemAddable(newTableForClassification, (J) existingTable.getPrimary(), resourceItem, classification, storeValue, enterprise);
                                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                    return session.insert(newTableForClassification)
                                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                    .onFailure().recoverWithItem(0L))
                                                            .replaceWith((IRelationshipValue<J, IResourceItem<?, ?>, ?>) newTableForClassification);
                                                });
                                    });
                        }));
    }

}

