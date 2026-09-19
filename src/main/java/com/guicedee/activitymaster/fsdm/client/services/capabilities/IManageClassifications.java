package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.enumerations.OrderByType;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipClassificationTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import com.guicedee.activitymaster.fsdm.client.services.exceptions.ClassificationException;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.get;

@SuppressWarnings({"DuplicatedCode", "rawtypes", "unchecked"})
public interface IManageClassifications<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>> {
    private String getClassificationsRelationshipTable() {
        String className = getClass().getCanonicalName() + "XClassification";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>,
            UUID, ?>> getClassificationsRelationshipClass() {
        String joinTableName = getClassificationsRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find classification linked class - " + joinTableName, e);
        }
    }

    // Convenience overload: Enum-based classification name
    default Uni<Long> numberOfClassifications(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfClassifications(session, classificationName.toString(), value, system, identityToken);
    }

    /**
     * Stateless count of active, in-date classification links for this entity matching the given name and
     * value. Uses a scalar {@code getCount()} (never hydrating the @Cacheable link entity) and the
     * enterprise from {@code system.getEnterprise()}; no {@code canRead} gating (counts the raw rows).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Long> numberOfClassifications(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> relationshipTable.builder(session)
                        .findLink((J) this, classification, value)
                        .inActiveRange()
                        .inDateRange()
                        .withEnterprise(system.getEnterprise())
                        .getCount());
    }

    /** Stateless variant of {@link #hasClassifications(Mutiny.StatelessSession, Enum, String, ISystems, UUID...)}. */
    default Uni<Boolean> hasClassifications(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return hasClassifications(session, classificationName.toString(), value, system, identityToken);
    }

    /** Stateless variant of {@link #hasClassifications(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
    default Uni<Boolean> hasClassifications(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfClassifications(session, classificationName, value, system, identityToken).map(count -> count > 0);
    }

    /** Stateless variant of {@link #findClassifications(Mutiny.StatelessSession, String, ISystems, UUID...)} — link rows are stateless-safe. */
    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.StatelessSession session, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> relationshipTable.builder(session)
                        .findLink((J) this, classification, null)
                        .inActiveRange()
                        .inDateRange()
                        .latestFirst()
                        .withEnterprise(system)
                        .getAll()
                        .map(list -> (List<IRelationshipValue<J, IClassification<?, ?>, ?>>) list));
    }

    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
        return relationshipTable.builder(session).findLink((J) this, null, null).inActiveRange().inDateRange().latestFirst().withEnterprise(system).getAll()
                .map(list -> (List<IRelationshipValue<J, IClassification<?, ?>, ?>>) list);
    }

    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.StatelessSession session, Enum<?> classificationName, ISystems<?, ?> system, UUID... identityToken) {
        return findClassification(session, classificationName.toString(), system, identityToken);
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.StatelessSession session, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> (Uni<IRelationshipValue<J, IClassification<?, ?>, ?>>) (Uni<?>) relationshipTable.builder(session)
                        .findLink((J) this, classification, null).inActiveRange().inDateRange().latestFirst().withEnterprise(system).canRead(system, identityToken).get());
    }

    default Uni<java.util.Map<String, String>> findClassificationValues(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken) {
        // Stateless-safe batched read: a join query that projects (ClassificationName, value) as SCALARS,
        // so the link's lazy @ManyToOne secondary classification name is NEVER navigated (navigating it
        // throws LazyInitializationException on a detached stateless entity). Mirrors the native-SQL join
        // used by IQueryBuilderClassifications.getClassificationsValuePivot.
        com.entityassist.RootEntity me = (com.entityassist.RootEntity) this;
        String myTableName = me.getTableName();
        String idColumnName = (String) me.getIdPair().getKey();
        Object myId = me.getIdPair().getValue();
        String joinTableName = myTableName + "XClassification";
        String targetTableName = "Classification.Classification";
        java.util.Set<com.entityassist.enumerations.ActiveFlag> activeFlags = com.entityassist.enumerations.ActiveFlag.getActiveRangeAndUp();
        String activeFlagsInList = com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags.listToSqlString(
                com.entityassist.enumerations.ActiveFlag.activeFlagToStrings(activeFlags));
        // Compare SCD effective windows against the same logical "now" the rows were written with
        // (convertToUTCDateTime(RootEntity.getNow())) rather than DB now() — the native query does not
        // auto-flush and the DB clock can trail a row written earlier in this same transaction. Mirror the
        // managed read: filter only the LINK row (its id, active flag, date range); join the classification
        // purely to project its name (reference data, always active).
        java.time.OffsetDateTime now = convertToUTCDateTime(com.entityassist.RootEntity.getNow());
        String sql = "select c.ClassificationName, ric.value " +
                "from " + joinTableName + " ric " +
                "join " + targetTableName + " c on ric.ClassificationID = c.ClassificationID " +
                "join dbo.ActiveFlag af on ric.ActiveFlagID = af.ActiveFlagID " +
                "where ric." + idColumnName + " = '" + myId + "' " +
                "and af.ActiveFlagName in (" + activeFlagsInList + ") " +
                "and ric.EffectiveFromDate <= :now and ric.EffectiveToDate >= :now";
        return session.createNativeQuery(sql, Object[].class)
                .setParameter("now", now)
                .getResultList()
                .map(rows -> {
                    java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
                    for (Object[] row : rows) {
                        if (row[0] != null) {
                            values.putIfAbsent(String.valueOf(row[0]), row[1] == null ? "" : String.valueOf(row[1]));
                        }
                    }
                    return values;
                });
    }

    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.StatelessSession session, String classificationName, int maxResults, ISystems<?, ?> system, UUID... identityToken) {
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> {
                    IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilderRelationshipClassification
                            = relationshipTable.builder(session)
                            .findLink((J) this, classification, null)
                            .inActiveRange()
                            .inDateRange()
                            .latestFirst()
                            .setMaxResults(maxResults)
                            .withEnterprise(system)
                            .canRead(system, identityToken);

                    //noinspection unchecked
                    return queryBuilderRelationshipClassification.getAll()
                            .map(list -> (List<IRelationshipValue<J, IClassification<?, ?>, ?>>) list);
                });
    }


    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.StatelessSession session, String classificationName, boolean distinct, ISystems<?, ?> system, UUID... identityToken) {
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> {
                    IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilderRelationshipClassification
                            = relationshipTable.builder(session)
                            .findLink((J) this, classification, null)
                            .inActiveRange()
                            .inDateRange()
                            .latestFirst()
                            .withEnterprise(system)
                            .canRead(system, identityToken);

                    //noinspection unchecked
                    return queryBuilderRelationshipClassification.getAll()
                            .map(list -> (List<IRelationshipValue<J, IClassification<?, ?>, ?>>) list);
                });
    }


    // Convenience overload: Enum-based classification name
    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.StatelessSession session, Enum<?> classificationName, ISystems<?, ?> system, UUID... identityToken) {
        return findClassifications(session, classificationName.toString(), system, identityToken);
    }

    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.StatelessSession session, String classificationName, boolean latest, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> {
                    final IClassification<?, ?> finalClassification = classification;
                    IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilderRelationshipClassification
                            = relationshipTable.builder(session)
                            .findLink((J) this, finalClassification, null)
                            .inActiveRange()
                            .inDateRange()
                            .latestFirst()
                            .withEnterprise(system)
                            .canRead(system, identityToken);

                    if (latest) {
                        queryBuilderRelationshipClassification.setMaxResults(1)
                                .orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"), OrderByType.DESC);
                    }

                    return (Uni<IRelationshipValue<J, IClassification<?, ?>, ?>>) (Uni<?>) queryBuilderRelationshipClassification.get();
                });
    }

    // ---- Stateless add (always-insert) classification link ----

    /** Stateless variant of {@link #addClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} — always inserts a fresh link. */
    default Uni<Void> addClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    /** Stateless variant of {@link #addClassification(Mutiny.StatelessSession, Enum, EnterpriseClassificationDataConcepts, String, ISystems, UUID...)}. */
    default Uni<Void> addClassification(Mutiny.StatelessSession session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName.toString(), concept, value, system, identityToken);
    }

    /** Stateless variant of {@link #addClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} — always inserts a fresh link. */
    default Uni<Void> addClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    /** Stateless variant of {@link #addClassification(Mutiny.StatelessSession, String, EnterpriseClassificationDataConcepts, String, ISystems, UUID...)}. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Void> addClassification(Mutiny.StatelessSession session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> sts =
                get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
        final com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise<?, ?> enterprise = system.getEnterprise();
        // The stateless prepped find resolves by name (concept-narrowing is a managed-only feature); name is the key.
        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                        .chain(activeFlag -> {
                            tableForClassification.setEnterpriseID(enterprise);
                            tableForClassification.setActiveFlagID(activeFlag);
                            tableForClassification.setSystemID(system);
                            tableForClassification.setOriginalSourceSystemID(system.getId());
                            tableForClassification.setOriginalSourceSystemUniqueID(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                            tableForClassification.setClassificationID(classification);
                            if (!Strings.isNullOrEmpty(value) && value.length() > 254) {
                                return Uni.createFrom().<Void>failure(new ClassificationException("Message value too long - " + value));
                            }
                            tableForClassification.setValue(value);
                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                            return configureForClassification(session, tableForClassification, classification, system)
                                    .chain(() -> session.insert(tableForClassification))
                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                            .onFailure().recoverWithItem(0L)
                                            .replaceWithVoid());
                        }));
    }

    // =============================================================================================
    // Stateless relationship-classification adds. These mirror addOrReuse/addOrUpdateClassification but
    // run entirely on a {@link Mutiny.StatelessSession}: existence is a scalar getCount() (never hydrating
    // the @Cacheable link entity), the link row is written with session.insert, and its default security is
    // provisioned via the stateless resolveDefaultGroupFolderTokens + createDefaultSecurity path (tolerant:
    // it is re-applied by the security bootstrap's batch apply-defaults phase). The enterprise reference is
    // taken from system.getEnterprise() (the prepped/created system carries it), so no managed lazy fetch is
    // needed. Both variants are find-or-insert (idempotent) and return Void — the install-chain callers
    // discard the link instance.
    // =============================================================================================

    /**
     * Stateless wiring of the link's owning back-reference (the stateful counterpart of the synchronous
     * {@code configureForClassification(...)} overloads). The default is a migration seam: an entity that
     * has not yet been taught to link classifications statelessly fails — the install chain prefers the
     * stateless overload where present and stays on the managed path otherwise.
     */
    default Uni<Void> configureForClassification(Mutiny.StatelessSession session, IWarehouseRelationshipClassificationTable linkTable, IClassification<?, ?> classificationValue, ISystems<?, ?> system) {
        return Uni.createFrom().failure(new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support stateless classification linking yet"));
    }

    /** Stateless find-or-insert classification link (Enum name). */
    default Uni<Void> addOrReuseClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrReuseClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    /** Stateless find-or-insert classification link (Enum name with concept). */
    default Uni<Void> addOrReuseClassification(Mutiny.StatelessSession session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrReuseClassification(session, classificationName.toString(), concept, value, system, identityToken);
    }

    /** Stateless find-or-insert classification link (String name). */
    default Uni<Void> addOrReuseClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrReuseClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    /** Stateless find-or-insert classification link (String name with concept). */
    default Uni<Void> addOrReuseClassification(Mutiny.StatelessSession session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrInsertClassificationStateless(session, classificationName, concept, value, system, identityToken);
    }

    /** Stateless add-or-update classification link (Enum name) — find-or-insert, idempotent on re-install. */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, searchValue, value, system, identityToken);
    }

    /** Stateless add-or-update classification link (Enum name with concept) — find-or-insert, idempotent on re-install. */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), concept, searchValue, value, system, identityToken);
    }

    /** Stateless add-or-update classification link (String name) — find-or-insert, idempotent on re-install. */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, searchValue, value, system, identityToken);
    }

    /** Stateless add-or-update classification link (String name with concept) — find-or-insert, idempotent on re-install. */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, String classificationName, EnterpriseClassificationDataConcepts concept, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrInsertClassificationStateless(session, classificationName, concept, value, system, identityToken);
    }

    // ---- Stateless add-or-update convenience overloads (value only — no explicit searchValue) ----

    /** Stateless add-or-update classification link (Enum name, value only). */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, null, value, system, identityToken);
    }

    /** Stateless add-or-update classification link (Enum name with concept, value only). */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), concept, null, value, system, identityToken);
    }

    /** Stateless add-or-update classification link (String name, value only). */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, null, value, system, identityToken);
    }

    /** Stateless add-or-update classification link (String name with concept, value only). */
    default Uni<Void> addOrUpdateClassification(Mutiny.StatelessSession session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName, concept, null, value, system, identityToken);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Uni<Void> addOrInsertClassificationStateless(Mutiny.StatelessSession session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise<?, ?> enterprise = system.getEnterprise();

        return classificationService.find(session, classificationName, concept, system, identityToken)
                .chain(classification -> tableForClassification.builder(session)
                        .findLink((J) this, classification, null)
                        .inActiveRange()
                        .inDateRange()
                        .withEnterprise(enterprise)
                        .getCount()
                        .chain(count -> {
                            if (count != null && count > 0) {
                                return Uni.createFrom().voidItem();
                            }
                            IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
                            return activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                                    .chain(activeFlag -> {
                                        tableForClassification.setEnterpriseID(enterprise);
                                        tableForClassification.setActiveFlagID(activeFlag);
                                        tableForClassification.setSystemID(system);
                                        tableForClassification.setOriginalSourceSystemID(system.getId());
                                        tableForClassification.setOriginalSourceSystemUniqueID(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                        tableForClassification.setClassificationID(classification);
                                        if (!Strings.isNullOrEmpty(value) && value.length() > 254) {
                                            return Uni.createFrom().<Void>failure(new ClassificationException("Message value too long - " + value));
                                        }
                                        tableForClassification.setValue(Strings.nullToEmpty(value));
                                        return configureForClassification(session, tableForClassification, classification, system)
                                                .chain(() -> session.insert(tableForClassification))
                                                .chain(() -> {
                                                    com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> sts =
                                                            get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
                                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                                    return sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                            .onFailure().recoverWithItem(0L)
                                                            .replaceWithVoid();
                                                });
                                    });
                        }));
    }

    // ---- Stateless SCD close mutations (archive / remove): bulk-UPDATE retire of the active link row ----

    /**
     * Stateless variant of {@link #archiveClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} —
     * closes the active classification link by stamping the <em>archived</em> active-flag + effective-to date
     * via a bulk HQL {@code UPDATE} ({@link SCDLinkMaintenance#retireActiveRow(Mutiny.StatelessSession, Object, UUID, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag, java.time.OffsetDateTime)}),
     * so it never hydrates a managed entity. No-op when the link is absent or its value differs.
     */
    default Uni<Void> archiveClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return archiveClassification(session, classificationName.toString(), value, system, identityToken);
    }

    /**
     * Stateless variant of {@link #archiveClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} —
     * closes the active classification link by stamping the <em>archived</em> active-flag + effective-to date
     * via a bulk HQL {@code UPDATE} ({@link SCDLinkMaintenance#retireActiveRow(Mutiny.StatelessSession, Object, UUID, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag, java.time.OffsetDateTime)}),
     * so it never hydrates a managed entity. No-op when the link is absent or its value differs.
     */
    default Uni<Void> archiveClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeClassificationStateless(session, classificationName, value, true, system, identityToken);
    }

    /**
     * Stateless variant of {@link #removeClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} —
     * closes the active classification link by stamping the <em>deleted</em> active-flag + effective-to date
     * via the same stateless bulk {@code UPDATE}. No-op when the link is absent or its value differs.
     */
    default Uni<Void> removeClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return removeClassification(session, classificationName.toString(), value, system, identityToken);
    }

    /**
     * Stateless variant of {@link #removeClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} —
     * closes the active classification link by stamping the <em>deleted</em> active-flag + effective-to date
     * via the same stateless bulk {@code UPDATE}. No-op when the link is absent or its value differs.
     */
    default Uni<Void> removeClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeClassificationStateless(session, classificationName, value, false, system, identityToken);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Uni<Void> closeClassificationStateless(Mutiny.StatelessSession session, String classificationName, String value, boolean archive, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        final com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise<?, ?> enterprise = system.getEnterprise();

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> tableForClassification.builder(session)
                        .findLink((J) this, classification, null)
                        .inActiveRange()
                        .inDateRange()
                        .withEnterprise(enterprise)
                        .get()
                        .map(r -> (Object) r)
                        .onFailure(NoResultException.class)
                        .recoverWithItem((Object) null)
                        .chain(existingObj -> {
                            IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> existing =
                                    (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) existingObj;
                            if (existing == null || !Strings.nullToEmpty(value).equals(existing.getValue())) {
                                return Uni.createFrom().voidItem();
                            }
                            Uni<? extends com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag<?, ?>> flagUni =
                                    archive ? flagService.getArchivedFlag(session, enterprise, identityToken)
                                            : flagService.getDeletedFlag(session, enterprise, identityToken);
                            // Close the row with a stateless full-row UPDATE by id (session.update) rather than a
                            // bulk HQL mutation: on a Mutiny.StatelessSession createMutationQuery(HQL) trips a JPMS
                            // access error (org.hibernate.orm.core does not export query.hql.spi to the reactive
                            // module). A stateless update writes every column (no dirty tracking), so the lazy
                            // effectiveToDate is persisted reliably.
                            return flagUni.chain(flag -> {
                                existing.setActiveFlagID(flag);
                                existing.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                return session.update(existing).replaceWithVoid();
                            });
                        }));
    }

    // ---- Stateless SCD update (retire + reinsert only when present) ----

    /**
     * Stateless variant of {@link #updateClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} —
     * SCD retire+reinsert only when the classification link already exists (no-op if absent or unchanged).
     */
    default Uni<Void> updateClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassification(session, classificationName.toString(), value, system, identityToken);
    }

    /**
     * Stateless variant of {@link #updateClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)} —
     * SCD retire+reinsert only when the classification link already exists (no-op if absent or unchanged).
     */
    default Uni<Void> updateClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassificationStateless(session, classificationName, value, system, identityToken);
    }

    /**
     * Concept-narrowed stateless variant. The stateless prepped {@code find} resolves by name (unique within a
     * system), so the {@code concept} narrows nothing extra here and is accepted only for API parity.
     */
    default Uni<Void> updateClassification(Mutiny.StatelessSession session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassification(session, classificationName.toString(), concept, value, system, identityToken);
    }

    /**
     * Concept-narrowed stateless variant. The stateless prepped {@code find} resolves by name (unique within a
     * system), so the {@code concept} narrows nothing extra here and is accepted only for API parity.
     */
    default Uni<Void> updateClassification(Mutiny.StatelessSession session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassificationStateless(session, classificationName, value, system, identityToken);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Uni<Void> updateClassificationStateless(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> sts =
                get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
        final com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise<?, ?> enterprise = system.getEnterprise();

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> get(getClassificationsRelationshipClass()).builder(session)
                        .findLink((J) this, classification, null)
                        .inActiveRange()
                        .inDateRange()
                        .withEnterprise(enterprise)
                        .get()
                        .map(r -> (Object) r)
                        .onFailure(NoResultException.class)
                        .recoverWithItem((Object) null)
                        .chain(resultObj -> {
                            IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> existing =
                                    (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) resultObj;
                            if (existing == null || Strings.nullToEmpty(value).equals(existing.getValue())) {
                                return Uni.createFrom().voidItem();
                            }
                            return flagService.getArchivedFlag(session, enterprise, identityToken)
                                    .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, (IWarehouseRelationshipTable) existing, existing.getId(), archivedFlag, convertToUTCDateTime(com.entityassist.RootEntity.getNow())))
                                    .chain(() -> flagService.getActiveFlag(session, enterprise, identityToken).chain(activeFlag -> {
                                        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> newRow =
                                                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
                                        newRow.setEnterpriseID(enterprise);
                                        newRow.setActiveFlagID(activeFlag);
                                        newRow.setSystemID(system);
                                        newRow.setOriginalSourceSystemID(system.getId());
                                        newRow.setOriginalSourceSystemUniqueID(existing.getId());
                                        newRow.setClassificationID(classification);
                                        newRow.setValue(Strings.nullToEmpty(value));
                                        newRow.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                        newRow.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                        com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newRow;
                                        return configureForClassification(session, newRow, classification, system)
                                                .chain(() -> session.insert(newRow))
                                                .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                        .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                        .onFailure().recoverWithItem(0L))
                                                .replaceWithVoid();
                                    }));
                        }));
    }
}

