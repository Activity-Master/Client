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

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> findRulesTypes(Mutiny.Session session, String classificationName, String rulesType, String value, ISystems<?, ?> system, UUID... identityToken) {
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
                            if (result == null) {
                                throw new NoSuchElementException("Rules type not found");
                            }
                            return (IRelationshipValue<J, IRulesType<?, ?>, ?>) result;
                        })
                );
    }

    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IRulesType<?, ?>, ?>>> findRulesTypesAll(Mutiny.Session session, String classificationName, String rulesType, String value, ISystems<?, ?> system, UUID... identityToken) {
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
                        .map(list -> (List<IRelationshipValue<J, IRulesType<?, ?>, ?>>) list)
                );
    }

    default Uni<Boolean> hasRuleTypes(Mutiny.Session session, String classificationName, String ruleTypeName, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfRuleTypes(session, classificationName, ruleTypeName, system, identityToken)
                .map(count -> count > 0);
    }

    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfRuleTypes(Mutiny.Session session, String classificationName, String ruleTypeName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);

        return rulesItemService.findRulesTypes(session, ruleTypeName, system, identityToken)
                .chain(ruleType -> tableForClassification.builder(session)
                        .findLink((J) this, ruleType, null)
                        .inActiveRange()
                        .withClassification(classificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .getCount()
                );
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addRuleTypes(Mutiny.Session session, String rulesType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        final String finalClassificationNameCopy = finalClassificationName;

        // Sequential chain instead of parallel operations
        return rulesItemService.findRulesTypes(session, rulesType, system, identityToken)
                .chain(ruleType -> classificationService.find(session, finalClassificationNameCopy, system, identityToken)
                        .chain(classification -> session.fetch(system)
                                .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                        .chain(enterprise -> {
                                            IActiveFlagService<?> activeFlagSvc = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
                                            return activeFlagSvc.getActiveFlag(session, enterprise)
                                        .map(activeFlag -> {
                                            tableForClassification.setEnterpriseID(enterprise);
                                            tableForClassification.setValue(value);
                                            tableForClassification.setSystemID(fetchedSystem);
                                            tableForClassification.setClassificationID(classification);
                                            tableForClassification.setOriginalSourceSystemID(fetchedSystem.getId());
                                            tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                            tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                            tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                            tableForClassification.setActiveFlagID(activeFlag);
                                            configureRuleTypeLinkValue(tableForClassification, (J) this, ruleType, classification, value, enterprise);

                                            return tableForClassification;
                                        });
                                        })))
                )
                .chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
                .chain(table ->
                        // Chain the createDefaultSecurity operation properly
                        table.createDefaultSecurity(session, system, identityToken)
                                .onFailure().invoke(error -> {
                                    // Log error but continue
                                    System.err.println("Error in createDefaultSecurity: " + error.getMessage());
                                })
                                .map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) table)
                );
    }

    @SuppressWarnings("rawtypes")
    void configureRuleTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IRulesType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);


    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addOrReuseRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);

        // Prepare classification name
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName)
                ? DefaultClassifications.NoClassification.toString()
                : classificationName;

        // First get the rule type
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .onItem().transformToUni(ruleType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, ruleType, searchValue)
                            .inActiveRange()
                            .withClassification(finalClassificationName, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .onFailure(NoResultException.class)
                            .recoverWithUni(() -> {
                                return (Uni) addRuleTypes(session, rulesTypeName, value, finalClassificationName, system, identityToken);
                            })
                            .chain(result -> {
                                // Cast the result to the correct type and return it
                                return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) result);
                            });
                });
    }

    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addOrUpdateRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        final String finalClassificationNameCopy = finalClassificationName;

        // Sequential chain instead of parallel operations
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> classificationService.find(session, finalClassificationNameCopy, system, identityToken)
                        .chain(classification -> {
                            // Create a query to find the existing relationship
                            return tableForClassification.builder(session)
                                    .findLink((J) this, ruleType, searchValue)
                                    .inActiveRange()
                                    .withClassification(finalClassificationNameCopy, system)
                                    .inDateRange()
                                    .canRead(system, identityToken)
                                    .get()
                                    .onFailure(NoResultException.class)
                                    .recoverWithUni(() -> {
                                        return session.fetch(system)
                                                .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                        .chain(enterprise -> {
                                                            IActiveFlagService<?> activeFlagSvc = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
                                                            return activeFlagSvc.getActiveFlag(session, enterprise)
                                                        .chain(activeFlag -> {
                                                            tableForClassification.setEnterpriseID(enterprise);
                                                            tableForClassification.setValue(value);
                                                            tableForClassification.setSystemID(fetchedSystem);
                                                            tableForClassification.setOriginalSourceSystemID(fetchedSystem.getId());
                                                            tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                                            tableForClassification.setActiveFlagID(activeFlag);
                                                            configureRuleTypeLinkValue(tableForClassification, (J) this, ruleType, classification, value, enterprise);

                                                            return (Uni) Uni.createFrom().item(tableForClassification)
                                                                    .chain(table -> {
                                                                        return session.persist(table).replaceWith(Uni.createFrom().item(table));
                                                                    })
                                                                    .chain(table -> {
                                                                        return table.createDefaultSecurity(session, system, identityToken)
                                                                                .onFailure().invoke(error -> {
                                                                                    // Log error but continue
                                                                                    System.err.println("Error in createDefaultSecurity: " + error.getMessage());
                                                                                })
                                                                                .map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) table);
                                                                    });
                                                        });
                                                        }));
                                    })
                                    .chain(result -> {
                                        // Cast the result to the correct type
                                        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable =
                                                (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

                                        // If the value is the same, return the existing relation
                                        if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                            return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
                                        }

                                        // Otherwise, update the relation
                                        ISystems<?, ?> originalSystem = existingTable.getSystemID();
                                        IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                        return session.fetch(system)
                                                .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                        .chain(systemEnterprise -> session.fetch(originalSystem)
                                                                .chain(fetchedOriginalSystem -> session.fetch(fetchedOriginalSystem.getEnterpriseID())
                                                                        .chain(originalEnterprise -> flagService.getArchivedFlag(session, systemEnterprise, identityToken)
                                                                                .chain(archivedFlag -> {
                                                                                    // Retire the current active row via a bulk UPDATE (bypasses the persistence context) so it
                                                                                    // is closed without detaching the managed entity, which would corrupt the following insert.
                                                                                    return SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag,
                                                                                            convertToUTCDateTime(RootEntity.getNow()));
                                                                                })
                                                                                .chain(() -> {
                                                                                    IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getRuleTypeRelationshipClass());
                                                                                    newTableForClassification.setId(null);
                                                                                    newTableForClassification.setSystemID(system);
                                                                                    newTableForClassification.setOriginalSourceSystemID(originalSystem.getId());
                                                                                    newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
                                                                                    newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                                    newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                                    newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                                                                    newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));

                                                                                    return flagService.getActiveFlag(session, originalEnterprise, identityToken)
                                                                                            .map(activeFlag -> {
                                                                                                newTableForClassification.setActiveFlagID(activeFlag);
                                                                                                newTableForClassification.setValue(value);
                                                                                                newTableForClassification.setEnterpriseID(systemEnterprise);
                                                                                                configureRuleTypeLinkValue(newTableForClassification, (J) this, ruleType, classification, value, systemEnterprise);
                                                                                                return newTableForClassification;
                                                                                            });
                                                                                })
                                                                                .chain(newTable -> {
                                                                                    return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
                                                                                })
                                                                                .chain(newTable -> {
                                                                                    // Chain the createDefaultSecurity operation properly
                                                                                    return newTable.createDefaultSecurity(session, originalSystem, identityToken)
                                                                                            .onFailure().invoke(error -> {
                                                                                                // Log error but continue
                                                                                                System.err.println("Error in createDefaultSecurity: " + error.getMessage());
                                                                                            })
                                                                                            .map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) newTable);
                                                                                })))));
                                    });
                        })
                );
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> updateRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        final String finalClassificationNameCopy = finalClassificationName;

        // Sequential chain instead of parallel operations
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> classificationService.find(session, finalClassificationNameCopy, system, identityToken)
                        .chain(classification -> {
                            // Create a query to find the existing relationship
                            return tableForClassification.builder(session)
                                    .findLink((J) this, ruleType, searchValue)
                                    .inActiveRange()
                                    .withClassification(finalClassificationNameCopy, system)
                                    .inDateRange()
                                    .canRead(system, identityToken)
                                    .get()
                                    .chain(result -> {
                                        // If result is null, do nothing
                                        if (result == null) {
                                            return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
                                        }

                                        // Cast the result to the correct type
                                        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable =
                                                (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

                                        // If the value is the same, return the existing relation
                                        if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                            return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
                                        }

                                        // Otherwise, update the relation
                                        ISystems<?, ?> originalSystem = existingTable.getSystemID();
                                        IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                        return session.fetch(system)
                                                .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                        .chain(systemEnterprise -> session.fetch(originalSystem)
                                                                .chain(fetchedOriginalSystem -> session.fetch(fetchedOriginalSystem.getEnterpriseID())
                                                                        .chain(originalEnterprise -> flagService.getArchivedFlag(session, systemEnterprise, identityToken)
                                                                                .chain(archivedFlag -> {
                                                                                    // Retire the current active row via a bulk UPDATE (bypasses the persistence context) so it
                                                                                    // is closed without detaching the managed entity, which would corrupt the following insert.
                                                                                    return SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag,
                                                                                            convertToUTCDateTime(RootEntity.getNow()));
                                                                                })
                                                                                .chain(() -> {
                                                                                    IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getRuleTypeRelationshipClass());
                                                                                    newTableForClassification.setId(null);
                                                                                    newTableForClassification.setSystemID(system);
                                                                                    newTableForClassification.setOriginalSourceSystemID(originalSystem.getId());
                                                                                    newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
                                                                                    newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                                    newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                                    newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                                                                    newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));

                                                                                    return flagService.getActiveFlag(session, originalEnterprise, identityToken)
                                                                                            .map(activeFlag -> {
                                                                                                newTableForClassification.setActiveFlagID(activeFlag);
                                                                                                newTableForClassification.setValue(value);
                                                                                                newTableForClassification.setEnterpriseID(systemEnterprise);
                                                                                                configureRuleTypeLinkValue(newTableForClassification, (J) this, ruleType, classification, value, systemEnterprise);
                                                                                                return newTableForClassification;
                                                                                            });
                                                                                })
                                                                                .chain(newTable -> {
                                                                                    return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
                                                                                })
                                                                                .chain(newTable -> {
                                                                                    // Chain the createDefaultSecurity operation properly
                                                                                    return newTable.createDefaultSecurity(session, originalSystem, identityToken)
                                                                                            .onFailure().invoke(error -> {
                                                                                                // Log error but continue
                                                                                                System.err.println("Error in createDefaultSecurity: " + error.getMessage());
                                                                                            })
                                                                                            .map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) newTable);
                                                                                })))));
                                    });
                        })
                );
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> expireRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        final String finalClassificationNameCopy = finalClassificationName;
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, ruleType, searchValue)
                            .inActiveRange()
                            .withClassification(finalClassificationNameCopy, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(result -> {
                                // If result is null, do nothing
                                if (result == null) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
                                }

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
                                }

                                // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                session.detach(existingTable);
                                existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                return session.merge(existingTable);
                            });
                });
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> archiveRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        final String finalClassificationNameCopy = finalClassificationName;
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, ruleType, searchValue)
                            .inActiveRange()
                            .withClassification(finalClassificationNameCopy, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(result -> {
                                // If result is null, do nothing
                                if (result == null) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
                                }

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
                                }

                                // Otherwise, archive the relation
                                IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                return session.fetch(system)
                                        .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                .chain(enterprise -> flagService.getArchivedFlag(session, enterprise, identityToken)
                                                        .chain(archivedFlag -> {
                                                            // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                                            // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                                            session.detach(existingTable);
                                                            existingTable.setActiveFlagID(archivedFlag);
                                                            existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                                            return session.merge(existingTable);
                                                        })));
                            });
                });
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> removeRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
        IRulesService<?> rulesItemService = get(IRulesService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        final String finalClassificationNameCopy = finalClassificationName;
        return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
                .chain(ruleType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, ruleType, searchValue)
                            .inActiveRange()
                            .withClassification(finalClassificationNameCopy, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(result -> {
                                // If result is null, do nothing
                                if (result == null) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
                                }

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
                                }

                                // Otherwise, remove the relation
                                IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                return session.fetch(system)
                                        .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                .chain(enterprise -> flagService.getDeletedFlag(session, enterprise, identityToken)
                                                        .chain(deletedFlag -> {
                                                            // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                                            // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                                            session.detach(existingTable);
                                                            existingTable.setActiveFlagID(deletedFlag);
                                                            existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                                            return session.merge(existingTable);
                                                        })));
                            });
                });
    }

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

    /** Stateless variant of {@link #expireRuleTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> expireRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeRuleTypesStateless(session, rulesTypeName, classificationName, searchValue, value, 0, system, identityToken);
    }

    /** Stateless variant of {@link #archiveRuleTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> archiveRuleTypes(Mutiny.StatelessSession session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeRuleTypesStateless(session, rulesTypeName, classificationName, searchValue, value, 1, system, identityToken);
    }

    /** Stateless variant of {@link #removeRuleTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
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
     * Stateless variant of {@link #updateRuleTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}
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
