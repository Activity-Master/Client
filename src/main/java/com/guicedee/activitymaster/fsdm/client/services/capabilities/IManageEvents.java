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
     * Finds an event with the given event type, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> findEvent(Mutiny.Session session, IEventType<?, ?> eventType, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());

        return tableForClassification.builder(session)
                .findLink((J) this, null, value)
                .inActiveRange()
                .inDateRange()
                .canRead(system, identityToken)
                .get()
                .map(result -> {
                    if (result == null) {
                        throw new NoSuchElementException("Event not found");
                    }
                    return (IRelationshipValue<J, IEvent<?, ?>, ?>) result;
                });
    }

    /**
     * Finds all events with the given event type and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IEvent<?, ?>, ?>>> findEventsAll(Mutiny.Session session, IEventType<?, ?> eventType, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());

        return tableForClassification.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .inDateRange()
                .canRead(system, identityToken)
                .getAll()
                .map(list -> (List<IRelationshipValue<J, IEvent<?, ?>, ?>>) list);
    }

    /**
     * Checks if the entity has events with the given event item type name and system.
     */
    default Uni<Boolean> hasEvents(Mutiny.Session session, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfEvents(session, eventItemTypeName, system, identityToken)
                .map(count -> count > 0);
    }

    /**
     * Gets the number of events with the given event item type name and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfEvents(Mutiny.Session session, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        return tableForClassification.builder(session)
                .findLink((J) this, null, null)
                .inActiveRange()
                .inDateRange()
                .canRead(system, identityToken)
                .getCount();
    }

    /**
     * Adds an event with the given event type, value, classification name, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addEvents(Mutiny.Session session, String eventType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        return classificationService.find(session, finalClassificationName, system, identityToken)
                .chain(classification -> session.fetch(system).chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                        .chain(enterprise -> {
                            IActiveFlagService<?> activeFlagSvc = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
                            return activeFlagSvc.getActiveFlag(session, enterprise)
                                .map(activeFlag -> {
                                    tableForClassification.setEnterpriseID(enterprise);
                                    tableForClassification.setValue(value);
                                    tableForClassification.setSystemID(system);
                                    tableForClassification.setClassificationID(classification);
                                    tableForClassification.setOriginalSourceSystemID(system.getId());
                                    tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                    tableForClassification.setActiveFlagID(activeFlag);
                                    configureEventLinkValue(tableForClassification, (J) this, null, classification, value, enterprise);

                                    return tableForClassification;
                                });
                        })))
                .chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
                .chain(table -> {
                    // Chain the security setup operation
                    return table.createDefaultSecurity(session, system, identityToken)
                            .onFailure().recoverWithNull()  // Continue even if security setup fails
                            .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) table));
                });
    }

    /**
     * Configures an event link value.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    void configureEventLinkValue(IWarehouseRelationshipTable linkTable, J primary, IEvent<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

    /**
     * Adds or reuses an event with the given event type name, classification name, search value, value, and system.
     */
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addOrReuseEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);

        // Prepare classification name
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName)
                ? DefaultClassifications.NoClassification.toString()
                : classificationName;

        // First get the event type
        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .onItem().transformToUni(eventItemType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, null, searchValue)
                            .inActiveRange()
                            .withClassification(finalClassificationName, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .onFailure(NoResultException.class)
                            .recoverWithUni(() -> {
                                return (Uni) addEvents(session, eventTypeName, value, finalClassificationName, system, identityToken);
                            })
                            .chain(result -> {
                                // Cast the result to the correct type and return it
                                return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) result);
                            });
                });
    }

    /**
     * Adds or updates an event with the given event type name, classification name, search value, value, and system.
     */
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addOrUpdateEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        // Create a final copy for use in lambda
        final String lambdaClassificationName = finalClassificationName;

        return Uni.combine().all().unis(
                        eventService.findEventType(session, eventTypeName, system, identityToken),
                        classificationService.find(session, finalClassificationName, system, identityToken)
                ).asTuple()
                .chain(tuple -> {
                    IEventType<?, ?> eventItemType = tuple.getItem1();
                    IClassification<?, ?> classification = tuple.getItem2();

                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, null, searchValue)
                            .inActiveRange()
                            .withClassification(lambdaClassificationName, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .onFailure(NoResultException.class)
                            .recoverWithUni(() -> {
                                return session.fetch(system).chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                        .chain(enterprise -> {
                                            IActiveFlagService<?> activeFlagSvc = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
                                            return activeFlagSvc.getActiveFlag(session, enterprise)
                                                .chain(activeFlag -> {
                                                    tableForClassification.setEnterpriseID(enterprise);
                                                    tableForClassification.setValue(value);
                                                    tableForClassification.setSystemID(system);
                                                    tableForClassification.setOriginalSourceSystemID(system.getId());
                                                    tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                                    tableForClassification.setActiveFlagID(activeFlag);
                                                    configureEventLinkValue(tableForClassification, (J) this, null, classification, value, enterprise);

                                                    return (Uni) Uni.createFrom().item(tableForClassification)
                                                            .chain(table -> {
                                                                return session.persist(table).replaceWith(Uni.createFrom().item(table));
                                                            })
                                                            .chain(table -> {
                                                                return table.createDefaultSecurity(session, system, identityToken)
                                                                        .onFailure().recoverWithNull()
                                                                        .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) table));
                                                            });
                                                });
                                        }));
                            })
                            .chain(result -> {

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable);
                                }

                                // Otherwise, update the relation
                                ISystems<?, ?> originalSystem = existingTable.getSystemID();
                                IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                return session.fetch(system).chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                        .chain(systemEnterprise -> session.fetch(originalSystem).chain(fetchedOriginalSystem -> session.fetch(fetchedOriginalSystem.getEnterpriseID())
                                                .chain(originalEnterprise -> flagService.getArchivedFlag(session, systemEnterprise, identityToken)
                                                        .chain(archivedFlag -> {
                                                            // Retire the current active row via a bulk UPDATE (bypasses the persistence context) so it
                                                            // is closed without detaching the managed entity, which would corrupt the following insert.
                                                            return SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag,
                                                                    convertToUTCDateTime(RootEntity.getNow()));
                                                        })
                                                        .chain(() -> {
                                                            IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> newTableForClassification = get(getEventRelationshipClass());
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
                                                                        configureEventLinkValue(newTableForClassification, (J) this, null, classification, value, systemEnterprise);
                                                                        return newTableForClassification;
                                                                    });
                                                        })
                                                        .chain(newTable -> {
                                                            return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
                                                        })
                                                        .chain(newTable -> {
                                                            // Chain the security setup operation
                                                            return newTable.createDefaultSecurity(session, originalSystem, identityToken)
                                                                    .onFailure().recoverWithNull()  // Continue even if security setup fails
                                                                    .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable));
                                                        })))));
                            });
                });
    }

    /**
     * Updates an event with the given event type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> updateEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        // Create a final copy for use in lambda
        final String lambdaClassificationName = finalClassificationName;

        return Uni.combine().all().unis(
                        eventService.findEventType(session, eventTypeName, system, identityToken),
                        classificationService.find(session, finalClassificationName, system, identityToken)
                ).asTuple()
                .chain(tuple -> {
                    IEventType<?, ?> eventItemType = tuple.getItem1();
                    IClassification<?, ?> classification = tuple.getItem2();

                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, null, searchValue)
                            .inActiveRange()
                            .withClassification(lambdaClassificationName, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(result -> {
                                // If result is null, do nothing
                                if (result == null) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) tableForClassification);
                                }

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable);
                                }

                                // Otherwise, update the relation
                                ISystems<?, ?> originalSystem = existingTable.getSystemID();
                                IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                return session.fetch(system).chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                        .chain(systemEnterprise -> session.fetch(originalSystem).chain(fetchedOriginalSystem -> session.fetch(fetchedOriginalSystem.getEnterpriseID())
                                                .chain(originalEnterprise -> flagService.getArchivedFlag(session, systemEnterprise, identityToken)
                                                        .chain(archivedFlag -> {
                                                            // Retire the current active row via a bulk UPDATE (bypasses the persistence context) so it
                                                            // is closed without detaching the managed entity, which would corrupt the following insert.
                                                            return SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag,
                                                                    convertToUTCDateTime(RootEntity.getNow()));
                                                        })
                                                        .chain(() -> {
                                                            IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> newTableForClassification = get(getEventRelationshipClass());
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
                                                                        configureEventLinkValue(newTableForClassification, (J) this, null, classification, value, systemEnterprise);
                                                                        return newTableForClassification;
                                                                    });
                                                        })
                                                        .chain(newTable -> {
                                                            return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
                                                        })
                                                        .chain(newTable -> {
                                                            // Chain the security setup operation
                                                            return newTable.createDefaultSecurity(session, originalSystem, identityToken)
                                                                    .onFailure().recoverWithNull()  // Continue even if security setup fails
                                                                    .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable));
                                                        })))));
                            });
                });
    }

    /**
     * Expires an event with the given event type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> expireEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        // Create a final copy for use in lambda
        final String lambdaClassificationName = finalClassificationName;

        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .chain(eventItemType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, null, searchValue)
                            .inActiveRange()
                            .withClassification(lambdaClassificationName, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(result -> {
                                // If result is null, do nothing
                                if (result == null) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) tableForClassification);
                                }

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable);
                                }

                                // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                session.detach(existingTable);
                                existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                return session.merge(existingTable);
                            });
                });
    }

    /**
     * Archives an event with the given event type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> archiveEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        // Create a final copy for use in lambda
        final String lambdaClassificationName = finalClassificationName;

        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .chain(eventItemType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, null, searchValue)
                            .inActiveRange()
                            .withClassification(lambdaClassificationName, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(result -> {
                                // If result is null, do nothing
                                if (result == null) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) tableForClassification);
                                }

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable);
                                }

                                // Otherwise, archive the relation
                                IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                return session.fetch(system).chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
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

    /**
     * Removes an event with the given event type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> removeEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
        IEventService<?> eventService = get(IEventService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        // Create a final copy for use in lambda
        final String lambdaClassificationName = finalClassificationName;

        return eventService.findEventType(session, eventTypeName, system, identityToken)
                .chain(eventItemType -> {
                    // Create a query to find the existing relationship
                    return tableForClassification.builder(session)
                            .findLink((J) this, null, searchValue)
                            .inActiveRange()
                            .withClassification(lambdaClassificationName, system)
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(result -> {
                                // If result is null, do nothing
                                if (result == null) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) tableForClassification);
                                }

                                // Cast the result to the correct type
                                IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> existingTable =
                                        (IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>) result;

                                // If the value is the same, return the existing relation
                                if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                    return Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) existingTable);
                                }

                                // Otherwise, remove the relation
                                IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                return session.fetch(system).chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
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
    // Stateless (Mutiny.StatelessSession) twins of the read + create family (add / addOrReuse /
    // addOrUpdate). Reads verbatim; writes use session.insert + system.getEnterprise() + the stateless
    // resolveDefaultGroupFolderTokens/createDefaultSecurity path. The unused findEventType lookup is
    // skipped (its result was discarded in the managed flow). The pure-close mutations (expire / archive /
    // remove) are twinned below via a stateless full-row session.update (the managed merge/detach path and
    // the SCD-versioning update() are not stateless-portable: createMutationQuery(HQL) is blocked on a
    // stateless session, and merge requires a persistence context).
    // =============================================================================================

    /** Stateless variant of {@link #expireEvents(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> expireEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventStateless(session, eventTypeName, classificationName, searchValue, value, 0, system, identityToken);
    }

    /** Stateless variant of {@link #archiveEvents(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> archiveEvents(Mutiny.StatelessSession session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeEventStateless(session, eventTypeName, classificationName, searchValue, value, 1, system, identityToken);
    }

    /** Stateless variant of {@link #removeEvents(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
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
     * Stateless variant of {@link #updateEvents(Mutiny.Session, String, String, String, String, ISystems, UUID...)}
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

