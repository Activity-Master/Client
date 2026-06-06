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
}

