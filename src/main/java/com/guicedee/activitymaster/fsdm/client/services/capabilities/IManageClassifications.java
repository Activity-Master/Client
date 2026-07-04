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

    default Uni<Boolean> hasClassifications(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return hasClassifications(session, classificationName.toString(), value, system, identityToken);
    }

    default Uni<Boolean> hasClassifications(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfClassifications(session, classificationName, value, system, identityToken)
                .map(count -> count > 0);
    }

    default Uni<Long> numberOfClassifications(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> relationshipTable.builder(session)
                        .findLink((J) this, classification, value)
                        .inActiveRange()
                        .inDateRange()
                        .canRead(system, identityToken)
                        .getCount());
    }

    // Convenience overload: Enum-based classification name
    default Uni<Long> numberOfClassifications(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
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

    /** Stateless variant of {@link #hasClassifications(Mutiny.Session, Enum, String, ISystems, UUID...)}. */
    default Uni<Boolean> hasClassifications(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return hasClassifications(session, classificationName.toString(), value, system, identityToken);
    }

    /** Stateless variant of {@link #hasClassifications(Mutiny.Session, String, String, ISystems, UUID...)}. */
    default Uni<Boolean> hasClassifications(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfClassifications(session, classificationName, value, system, identityToken).map(count -> count > 0);
    }

    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
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

    /** Stateless variant of {@link #findClassifications(Mutiny.Session, String, ISystems, UUID...)} — link rows are stateless-safe. */
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

    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, String classificationName, int maxResults, ISystems<?, ?> system, UUID... identityToken) {
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


    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, String classificationName, boolean distinct, ISystems<?, ?> system, UUID... identityToken) {
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
    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, Enum<?> classificationName, ISystems<?, ?> system, UUID... identityToken) {
        return findClassifications(session, classificationName.toString(), system, identityToken);
    }

    default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilderRelationshipClassification
                = relationshipTable.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .inDateRange()
                .latestFirst()
                .withEnterprise(system)
                .canRead(system, identityToken);

        //noinspection unchecked
        return queryBuilderRelationshipClassification.getAll()
                .map(list -> (List<IRelationshipValue<J, IClassification<?, ?>, ?>>) list);
    }

    /**
     * Bulk-reads every classification attached to this entity in a single security-checked query and
     * returns them as a {@code classification-name -> value} map.
     * <p>
     * This is the batched read path for hydration flows (e.g. GraphQL/REST DTO assembly) that would
     * otherwise chain a separate {@link #findClassification} round-trip per field. Because a single
     * {@link Mutiny.Session} may only run one operation at a time (Hibernate Reactive forbids parallel
     * operations on a session), the classifications cannot be fetched concurrently — instead they are
     * loaded once via {@link #findClassifications(Mutiny.Session, ISystems, UUID...)} (which already
     * applies {@code canRead}, active-range, date-range and latest-first ordering) and each lazy
     * secondary classification is fetched to resolve its name. Where a classification appears more
     * than once, the latest value wins (the list is ordered latest-first).
     *
     * @param session       The reactive session
     * @param system        The system the read is scoped to
     * @param identityToken Optional security identity tokens
     * @return A Uni emitting a map of classification name to its (latest) value
     */
    default Uni<java.util.Map<String, String>> findClassificationValues(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken) {
        return findClassifications(session, system, identityToken)
                .chain(links -> {
                    java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
                    Uni<Void> chain = Uni.createFrom().voidItem();
                    for (IRelationshipValue<J, IClassification<?, ?>, ?> link : links) {
                        chain = chain.chain(() -> session.fetch(link.getSecondary())
                                .invoke(classification -> {
                                    if (classification != null && classification.getName() != null) {
                                        // latest-first ordering: keep the first (latest) value seen per name
                                        values.putIfAbsent(classification.getName(), link.getValue());
                                    }
                                })
                                .replaceWithVoid());
                    }
                    return chain.replaceWith(values);
                });
    }

    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.Session session, Enum<?> classificationName, ISystems<?, ?> system, UUID... identityToken) {
        return findClassification(session, classificationName.toString(), system, identityToken);
    }

    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.Session session, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        return findClassification(session, classificationName, false, system, identityToken);
    }

    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.Session session, String classificationName, boolean latest, ISystems<?, ?> system, UUID... identityToken) {
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

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addClassification(Mutiny.Session session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName.toString(), concept, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addClassification(Mutiny.Session session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {

        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, concept, system, identityToken)
                .onFailure(NoResultException.class)
                .invoke(err -> {
                    LogManager.getLogger(IManageClassifications.class).error("Classification not found: " + classificationName + " in concept " + concept, err);
                })
                .chain(classification -> {
                    return session.fetch(system)
                            .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                    .chain(enterprise -> {
                                        tableForClassification.setEnterpriseID(enterprise);
                                        IActiveFlagService<?> activeFlagSvc = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
                                        return activeFlagSvc.getActiveFlag(session, enterprise);
                                    })
                                    .chain(activeFlag -> {
                                        tableForClassification.setActiveFlagID(activeFlag);
                                        tableForClassification.setSystemID(fetchedSystem);
                                        tableForClassification.setOriginalSourceSystemID(fetchedSystem.getId());
                                        tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                        tableForClassification.setClassificationID(classification);
                                        if (!Strings.isNullOrEmpty(value) && value.length() > 254) {
                                            throw new ClassificationException("Message value too long - " + value);
                                        }
                                        tableForClassification.setValue(value);

                                        return configureForClassification(session, tableForClassification, classification, fetchedSystem)
                                                .replaceWith(tableForClassification);
                                    }));
                })
                .chain(table -> session.persist(table)
                        .replaceWith(Uni.createFrom()
                                .item(table)))
                .chain(table -> {
                    // In a bulk-load context the link row is recorded for batched/stateless security at the
                    // end of the phase; otherwise create its default security per-row (single-entity create).
                    if (com.guicedee.activitymaster.fsdm.client.services.DefaultSecurityCollector.isActive(session)) {
                        com.guicedee.activitymaster.fsdm.client.services.DefaultSecurityCollector.record(session, table);
                        return Uni.createFrom().item(table);
                    }
                    return table.createDefaultSecurity(session, system, identityToken)
                            .map(v -> table); // Return the table after security operation completes
                });
    }

    // ---- Stateless add (always-insert) classification link ----

    /** Stateless variant of {@link #addClassification(Mutiny.Session, String, String, ISystems, UUID...)} — always inserts a fresh link. */
    default Uni<Void> addClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    /** Stateless variant of {@link #addClassification(Mutiny.Session, Enum, EnterpriseClassificationDataConcepts, String, ISystems, UUID...)}. */
    default Uni<Void> addClassification(Mutiny.StatelessSession session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName.toString(), concept, value, system, identityToken);
    }

    /** Stateless variant of {@link #addClassification(Mutiny.Session, String, String, ISystems, UUID...)} — always inserts a fresh link. */
    default Uni<Void> addClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    /** Stateless variant of {@link #addClassification(Mutiny.Session, String, EnterpriseClassificationDataConcepts, String, ISystems, UUID...)}. */
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

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, null, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, Enum<?> classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, searchValue, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), concept, null, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName.toString(), concept, searchValue, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, null, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, searchValue, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrUpdateClassification(session, classificationName, concept, null, value, system, identityToken);
    }

    /**
     * Adds or updates a classification link, scoping the underlying classification lookup to the supplied data concept.
     * <p>
     * Classification names are frequently duplicated across concepts (for example ISO codes reused by the Languages,
     * Country and Currency concepts). Passing the {@code concept} disambiguates the lookup so the correct classification
     * is resolved instead of collapsing onto {@code NoClassificationDataConceptName}. Callers that do not supply a
     * concept default to {@link EnterpriseClassificationDataConcepts#NoClassificationDataConceptName}.
     */
    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, String classificationName, EnterpriseClassificationDataConcepts concept, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return (Uni) classificationService.find(session, classificationName, concept, system, identityToken)
                .chain(classification -> {
                    IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilder =
                            tableForClassification.builder(session)
                                    .findLink((J) this, classification, searchValue)
                                    .inActiveRange()
                                    .inDateRange()
                                    .latestFirst()
                                    .canRead(system, identityToken);

                    return (Uni<?>)
                            queryBuilder.get()
                                    .onFailure(NoResultException.class)
                                    .recoverWithUni(
                                            () -> {
                                                return (Uni) addClassification(session, classificationName, concept, value, system, identityToken);
                                            }
                                    )
                                    .onItem()
                                    .call(a -> {
                                        return updateClassification(session, classificationName, concept, value, system, identityToken);
                                    });
                });
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrReuseClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrReuseClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrReuseClassification(Mutiny.Session session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrReuseClassification(session, classificationName.toString(), concept, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrReuseClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return addOrReuseClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }


    /**
     * Finds an existing classification link or creates one, scoping the underlying classification lookup to the
     * supplied data concept so duplicate names across concepts resolve correctly. Callers that do not supply a concept
     * default to {@link EnterpriseClassificationDataConcepts#NoClassificationDataConceptName}.
     */
    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrReuseClassification(Mutiny.Session session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {

        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, concept, system, identityToken)
                .chain(classification -> {
                    return session.fetch(system)
                            .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                    .chain(enterprise -> {
                                        return (Uni) tableForClassification.builder(session)
                                                .findLink((J) this, classification, null)
                                                .inActiveRange()
                                                .inDateRange()
                                                .withEnterprise(enterprise)
                                                .canRead(system, identityToken)
                                                .get()
                                                .onFailure(NoResultException.class)
                                                .recoverWithUni(() -> {
                                                    return (Uni) addClassification(session, classificationName, concept, value, system, identityToken);
                                                })
                                                .onItem()
                                                .call(a -> {
                                                    return Uni.createFrom()
                                                            .item((IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) a);
                                                });
                                    }));
                });
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

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> updateClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassification(session, classificationName.toString(), EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> updateClassification(Mutiny.Session session, Enum<?> classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassification(session, classificationName.toString(), concept, value, system, identityToken);
    }

    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> updateClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
    }

    @SuppressWarnings("unchecked")
    default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> updateClassification(Mutiny.Session session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, ?, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, concept, system, identityToken)
                .chain(classification -> {
                    final IClassification<?, ?> finalClassification = classification;
                    return tableForClassification.builder(session)
                            .findLink((J) this, finalClassification, null)
                            .inActiveRange()
                            .inDateRange()
                            .latestFirst()
                            .canRead(system, identityToken)
                            .get()
                            .chain(existingTable -> {
                                if (existingTable == null) {
                                    return Uni.createFrom()
                                            .failure(new ClassificationException("Unable to find classification"));
                                } else {
                                    final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, ?, ?> finalTableForClassification =
                                            (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, ?, ?>) existingTable;
                                    if (Strings.nullToEmpty(value)
                                            .equals(existingTable.getValue())) {
                                        return Uni.createFrom()
                                                .item((IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) finalTableForClassification);
                                    }

                                    final ISystems<?, ?> originalSystem = finalTableForClassification.getSystemID();
                                    IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                    return session.fetch(system)
                                            .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                    .chain(systemEnterprise -> session.fetch(originalSystem)
                                                            .chain(fetchedOriginalSystem -> session.fetch(fetchedOriginalSystem.getEnterpriseID())
                                                                    .chain(originalEnterprise -> flagService.getArchivedFlag(session, systemEnterprise, identityToken)
                                                                            .chain(archivedFlag -> {
                                                                                // Retire the current active row via a bulk UPDATE (bypasses the persistence context) so it
                                                                                // is closed without detaching the managed entity, which would corrupt the following insert.
                                                                                return SCDLinkMaintenance.retireActiveRow(session, finalTableForClassification, finalTableForClassification.getId(), archivedFlag,
                                                                                        convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                            })
                                                                            .chain(retiredCount -> {
                                                                                IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> newTableForClassification =
                                                                                        (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());

                                                                                newTableForClassification.setId(null);
                                                                                newTableForClassification.setClassificationID(finalTableForClassification.getClassificationID());
                                                                                newTableForClassification.setSystemID(system);
                                                                                newTableForClassification.setOriginalSourceSystemID(originalSystem.getId());
                                                                                newTableForClassification.setOriginalSourceSystemUniqueID(finalTableForClassification.getId());
                                                                                newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                                newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                                newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                                newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));

                                                                                return flagService.getActiveFlag(session, originalEnterprise, identityToken)
                                                                                        .chain(activeFlag -> {
                                                                                            newTableForClassification.setActiveFlagID(activeFlag);
                                                                                            newTableForClassification.setValue(value);
                                                                                            newTableForClassification.setEnterpriseID(systemEnterprise);

                                                                                            return configureForClassification(session, newTableForClassification, finalClassification, system)
                                                                                                    .replaceWith(newTableForClassification);
                                                                                        });
                                                                            }))))
                                                    .chain(newTable -> session.persist(newTable)
                                                            .replaceWith(Uni.createFrom()
                                                                    .item(newTable)))
                                                    .chain(newTable -> {
                                                        // Batch in a bulk-load context; otherwise per-row default security.
                                                        if (com.guicedee.activitymaster.fsdm.client.services.DefaultSecurityCollector.isActive(session)) {
                                                            com.guicedee.activitymaster.fsdm.client.services.DefaultSecurityCollector.record(session, (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable<?, ?, ?, ?>) newTable);
                                                            return Uni.createFrom().item((IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) newTable);
                                                        }
                                                        return newTable.createDefaultSecurity(session, originalSystem, identityToken)
                                                                .map(v -> (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) newTable); // Return the table after security operation completes
                                                    }));
                                }
                            });
                });
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> archiveClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return archiveClassification(session, classificationName.toString(), value, system, identityToken);
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> archiveClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> {
                    final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> initialTableForClassification = tableForClassification;
                    return tableForClassification.builder(session)
                            .findLink((J) this, classification, null)
                            .inActiveRange()
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(existingTable -> {
                                if (existingTable == null) {
                                    return Uni.createFrom()
                                            .item((IRelationshipValue<J, IClassification<?, ?>, ?>) initialTableForClassification);
                                } else {
                                    final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> finalTableForClassification =
                                            (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) existingTable;
                                    if (Strings.nullToEmpty(value)
                                            .equals(existingTable.getValue())) {
                                        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                        return session.fetch(system)
                                                .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                        .chain(enterprise -> flagService.getArchivedFlag(session, enterprise, identityToken)
                                                                .chain(archivedFlag -> {
                                                                    // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                                                    // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                                                    session.detach(finalTableForClassification);
                                                                    finalTableForClassification.setActiveFlagID(archivedFlag);
                                                                    finalTableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                    return session.merge(finalTableForClassification);
                                                                })));
                                    }

                                    // Value does not match; no-op (return the current link)
                                    return Uni.createFrom()
                                            .item((IRelationshipValue<J, IClassification<?, ?>, ?>) existingTable);
                                }
                            });
                });

    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> removeClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return removeClassification(session, classificationName.toString(), value, system, identityToken);
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> removeClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
                (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, system, identityToken)
                .chain(classification -> {
                    final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> initialTableForClassification = tableForClassification;
                    return tableForClassification.builder(session)
                            .findLink((J) this, classification, null)
                            .inActiveRange()
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(existingTable -> {
                                if (existingTable == null) {
                                    return Uni.createFrom()
                                            .item((IRelationshipValue<J, IClassification<?, ?>, ?>) initialTableForClassification);
                                } else {
                                    final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> finalTableForClassification =
                                            (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) existingTable;
                                    if (Strings.nullToEmpty(value)
                                            .equals(existingTable.getValue())) {
                                        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                        return session.fetch(system)
                                                .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                        .chain(enterprise -> flagService.getDeletedFlag(session, enterprise, identityToken)
                                                                .chain(deletedFlag -> {
                                                                    // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                                                    // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                                                    session.detach(finalTableForClassification);
                                                                    finalTableForClassification.setActiveFlagID(deletedFlag);
                                                                    finalTableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                    return session.merge(finalTableForClassification);
                                                                })));
                                    }

                                    // Value does not match; no-op (return the current link)
                                    return Uni.createFrom()
                                            .item((IRelationshipValue<J, IClassification<?, ?>, ?>) existingTable);
                                }
                            });
                });
    }

    /**
     * Reactive, non-blocking configuration of a classification link. Implementors that only need to
     * set fields can return {@code Uni.createFrom().voidItem()}. Implementors that must resolve data
     * reactively - e.g. a classification-&gt;classification link that looks up the NoClassification
     * record - return the resolving chain so the Vert.x event loop is never blocked with
     * {@code await().atMost(...)}.
     */
    Uni<Void> configureForClassification(Mutiny.Session session, IWarehouseRelationshipClassificationTable linkTable, IClassification<?, ?> classificationValue, ISystems<?, ?> system);

    // ---- Stateless SCD close mutations (archive / remove): bulk-UPDATE retire of the active link row ----

    /**
     * Stateless variant of {@link #archiveClassification(Mutiny.Session, String, String, ISystems, UUID...)} —
     * closes the active classification link by stamping the <em>archived</em> active-flag + effective-to date
     * via a bulk HQL {@code UPDATE} ({@link SCDLinkMaintenance#retireActiveRow(Mutiny.StatelessSession, Object, UUID, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag, java.time.OffsetDateTime)}),
     * so it never hydrates a managed entity. No-op when the link is absent or its value differs.
     */
    default Uni<Void> archiveClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return archiveClassification(session, classificationName.toString(), value, system, identityToken);
    }

    /**
     * Stateless variant of {@link #archiveClassification(Mutiny.Session, String, String, ISystems, UUID...)} —
     * closes the active classification link by stamping the <em>archived</em> active-flag + effective-to date
     * via a bulk HQL {@code UPDATE} ({@link SCDLinkMaintenance#retireActiveRow(Mutiny.StatelessSession, Object, UUID, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag, java.time.OffsetDateTime)}),
     * so it never hydrates a managed entity. No-op when the link is absent or its value differs.
     */
    default Uni<Void> archiveClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeClassificationStateless(session, classificationName, value, true, system, identityToken);
    }

    /**
     * Stateless variant of {@link #removeClassification(Mutiny.Session, String, String, ISystems, UUID...)} —
     * closes the active classification link by stamping the <em>deleted</em> active-flag + effective-to date
     * via the same stateless bulk {@code UPDATE}. No-op when the link is absent or its value differs.
     */
    default Uni<Void> removeClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return removeClassification(session, classificationName.toString(), value, system, identityToken);
    }

    /**
     * Stateless variant of {@link #removeClassification(Mutiny.Session, String, String, ISystems, UUID...)} —
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
     * Stateless variant of {@link #updateClassification(Mutiny.Session, String, String, ISystems, UUID...)} —
     * SCD retire+reinsert only when the classification link already exists (no-op if absent or unchanged).
     */
    default Uni<Void> updateClassification(Mutiny.StatelessSession session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken) {
        return updateClassification(session, classificationName.toString(), value, system, identityToken);
    }

    /**
     * Stateless variant of {@link #updateClassification(Mutiny.Session, String, String, ISystems, UUID...)} —
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

