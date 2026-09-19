package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType;
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
public interface IManageRuleTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>> {
    private String getResourceItemsRelationshipTable() {
        String className = getClass().getCanonicalName() + "XRulesType";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>> getRuleTypeRelationshipClass() {
        String joinTableName = getResourceItemsRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find ruleType linked class - " + joinTableName, e);
        }
    }

    @SuppressWarnings("rawtypes")
    void configureRuleTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IRulesType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);


    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins of the read + create family. Secondary IRulesType is
    // resolved via the stateless IRulesService.findRulesTypes; writes use session.insert +
    // system.getEnterprise() + the stateless default-security path. configureRuleTypeLinkValue is
    // session-free. update / expire / archive / remove (session.merge based) are not twinned here.
    // =============================================================================================

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> findRulesTypes(Mutiny.StatelessSession session, String classificationName, String rulesType, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        return rulesItemService.findRulesTypes(session, rulesType, system, identityToken)
                .chain(ruleType -> tableForClassification.builder(session)
                        .findLink((J) this, ruleType, value)
                        .inActiveRange()
                        .withClassification(classificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .map(result -> {
                            if (result == null) { throw new NoSuchElementException("Rules type not found"); }
                            return (IRelationshipValue<J, IRulesType<?, ?>, ?>) result;
                        }));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<List<IRelationshipValue<J, IRulesType<?, ?>, ?>>> findRulesTypesAll(Mutiny.StatelessSession session, String classificationName, String rulesType, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        if (rulesType == null) {
            return tableForClassification.builder(session)
                    .findLink((J) this, null, value)
                    .inActiveRange()
                    .withClassification(classificationName, system)
                    .inDateRange()
                    .canRead(system, identityToken)
                    .getAll()
                    .map(list -> (List<IRelationshipValue<J, IRulesType<?, ?>, ?>>) list);
        }
        return rulesItemService.findRulesTypes(session, rulesType, system, identityToken)
                .chain(ruleType -> tableForClassification.builder(session)
                        .findLink((J) this, ruleType, value)
                        .inActiveRange()
                        .withClassification(classificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .getAll()
                        .map(list -> (List<IRelationshipValue<J, IRulesType<?, ?>, ?>>) list));
    }

    default Uni<Boolean> hasRuleTypes(Mutiny.StatelessSession session, String classificationName, String ruleTypeName, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfRuleTypes(session, classificationName, ruleTypeName, system, identityToken).map(count -> count > 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Long> numberOfRuleTypes(Mutiny.StatelessSession session, String classificationName, String ruleTypeName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        return rulesItemService.findRulesTypes(session, ruleTypeName, system, identityToken)
                .chain(ruleType -> tableForClassification.builder(session)
                        .findLink((J) this, ruleType, null)
                        .inActiveRange()
                        .withClassification(classificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .getCount());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addRuleTypes(Mutiny.StatelessSession session, String rulesType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return rulesItemService.findRulesTypes(session, rulesType, system, identityToken)
                .chain(ruleType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                                .chain(activeFlag -> {
                                    tableForClassification.setEnterpriseID(enterprise);
                                    tableForClassification.setValue(value);
                                    tableForClassification.setSystemID(system);
                                    tableForClassification.setClassificationID(classification);
                                    tableForClassification.setOriginalSourceSystemID(system.getId());
                                    tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                    tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                    tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                    tableForClassification.setActiveFlagID(activeFlag);
                                    configureRuleTypeLinkValue(tableForClassification, (J) this, ruleType, classification, value, enterprise);
                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                    if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                                    return session.insert(tableForClassification)
                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                    .onFailure().recoverWithItem(0L))
                                            .replaceWith((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
                                })));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addOrReuseRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .onItem().transformToUni(ruleType -> tableForClassification.builder(session)
                        .findLink((J) this, ruleType, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .onFailure(NoResultException.class)
                        .recoverWithUni(() -> (Uni) addRuleTypes(session, rulesTypeName, value, finalClassificationName, system, identityToken))
                        .chain(result -> Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) result)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addOrUpdateRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> tableForClassification.builder(session)
                                .findLink((J) this, ruleType, searchValue)
                                .inActiveRange()
                                .withClassification(finalClassificationName, system)
                                .inDateRange()
                                .canRead(system, identityToken)
                                .get()
                                .onFailure(NoResultException.class)
                                .recoverWithUni(() -> (Uni) addRuleTypes(session, rulesTypeName, value, finalClassificationName, system, identityToken))
                                .chain(result -> {
                                    IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable =
                                            (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;
                                    if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                        return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
                                    }
                                    IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                    ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                                    return flagService.getArchivedFlag(session, enterprise, identityToken)
                                            .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                            .chain(() -> {
                                                IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getRuleTypeRelationshipClass());
                                                newTableForClassification.setId(java.util.UUID.randomUUID());
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
                                                            configureRuleTypeLinkValue(newTableForClassification, (J) this, ruleType, classification, value, enterprise);
                                                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                            return session.insert(newTableForClassification)
                                                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                            .onFailure().recoverWithItem(0L))
                                                                    .replaceWith((IRelationshipValue<J, IRulesType<?, ?>, ?>) newTableForClassification);
                                                        });
                                            });
                                })));
    }

    // ---- Stateless SCD close mutations (expire / archive / remove) via full-row session.update ----

    /** Stateless variant of {@link #expireRuleTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> expireRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeRuleTypesStateless(session, rulesTypeName, classificationName, searchValue, value, 0, system, identityToken);
    }

    /** Stateless variant of {@link #archiveRuleTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> archiveRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeRuleTypesStateless(session, rulesTypeName, classificationName, searchValue, value, 1, system, identityToken);
    }

    /** Stateless variant of {@link #removeRuleTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> removeRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeRuleTypesStateless(session, rulesTypeName, classificationName, searchValue, value, 2, system, identityToken);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Uni<Void> closeRuleTypesStateless(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, int mode, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;

        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> tableForClassification.builder(session)
                        .findLink((J) this, ruleType, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .map(r -> (Object) r)
                        .onFailure(NoResultException.class)
                        .recoverWithItem((Object) null)
                        .chain(resultObj -> {
                            IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existing =
                                    (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) resultObj;
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
     * Stateless variant of {@link #updateRuleTypes(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}
     * — SCD retire+reinsert <em>only when the link already exists</em> (no-op if absent, unlike addOrUpdate which
     * inserts). Retires the active row via the stateless {@code retireActiveRow} (full-row {@code session.update})
     * then inserts the new version + its default security.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Void> updateRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IRulesService<?> rulesItemService = get(IRulesService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> get(getRuleTypeRelationshipClass()).builder(session)
                                .findLink((J) this, ruleType, searchValue)
                                .inActiveRange()
                                .withClassification(finalClassificationName, system)
                                .inDateRange()
                                .canRead(system, identityToken)
                                .get()
                                .map(r -> (Object) r)
                                .onFailure(NoResultException.class)
                                .recoverWithItem((Object) null)
                                .chain(resultObj -> {
                                    IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existing =
                                            (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) resultObj;
                                    if (existing == null || Strings.nullToEmpty(value).equals(existing.getValue())) {
                                        return Uni.createFrom().voidItem();
                                    }
                                    return flagService.getArchivedFlag(session, enterprise, identityToken)
                                            .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existing, existing.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                            .chain(() -> {
                                                IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> newRow = get(getRuleTypeRelationshipClass());
                                                newRow.setId(java.util.UUID.randomUUID());
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
                                                    configureRuleTypeLinkValue(newRow, (J) this, ruleType, classification, value, enterprise);
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
