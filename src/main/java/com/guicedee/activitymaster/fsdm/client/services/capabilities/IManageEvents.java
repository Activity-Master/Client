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
public interface IManageEvents<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
	private String getEventRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XEvent";
		return className;
	}

	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>> getEventRelationshipClass()
	{
		String joinTableName = getEventRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find eventItemType linked class - " + joinTableName, e);
		}
	}

	/**
	 * Finds an event with the given event type, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> findEvent(Mutiny.Session session, IEventType<?,?> eventType, String value, ISystems<?,?> system, UUID... identityToken)
	{
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
	default Uni<List<IRelationshipValue<J, IEvent<?, ?>, ?>>> findEventsAll(Mutiny.Session session, IEventType<?,?> eventType, ISystems<?,?> system, UUID... identityToken)
	{
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
	default Uni<Boolean> hasEvents(Mutiny.Session session, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		return numberOfEvents(session, eventItemTypeName, system, identityToken)
				.map(count -> count > 0);
	}

	/**
	 * Gets the number of events with the given event item type name and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfEvents(Mutiny.Session session, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
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
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addEvents(Mutiny.Session session, String eventType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			return classificationService.find(session, finalClassificationName, system, identityToken)
					.map(classification -> {
						tableForClassification.setEnterpriseID(system.getEnterpriseID());
						tableForClassification.setValue(value);
						tableForClassification.setSystemID(system);
						tableForClassification.setClassificationID(classification);
						tableForClassification.setOriginalSourceSystemID(system.getId());
						tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
						tableForClassification.setActiveFlagID(system.getActiveFlagID());
						configureEventLinkValue(tableForClassification, (J) this, null, classification, value, system.getEnterpriseID());

						return tableForClassification;
					})
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
	 * 
	 * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
	 * It doesn't need to return a Uni as it's a synchronous operation.
	 */
	void configureEventLinkValue(IWarehouseRelationshipTable linkTable, J primary, IEvent<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

	/**
	 * Adds or reuses an event with the given event type name, classification name, search value, value, and system.
	 */
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addOrReuseEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
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
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> addOrUpdateEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
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
							tableForClassification.setEnterpriseID(system.getEnterpriseID());
							tableForClassification.setValue(value);
							tableForClassification.setSystemID(system);
							tableForClassification.setOriginalSourceSystemID(system.getId());
							tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
							tableForClassification.setActiveFlagID(system.getActiveFlagID());
							configureEventLinkValue(tableForClassification, (J) this, null, classification, value, system.getEnterpriseID());

							return (Uni) Uni.createFrom().item(tableForClassification)
									.chain(table -> {
										return session.persist(table).replaceWith(Uni.createFrom().item(table));
									})
									.chain(table -> {
										// Chain the security setup operation
										return table.createDefaultSecurity(session, system, identityToken)
											.onFailure().recoverWithNull()  // Continue even if security setup fails
											.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEvent<?, ?>, ?>) table));
									});
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

							return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
									.chain(archivedFlag -> {
										existingTable.setActiveFlagID(archivedFlag);
										existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
										return session.merge(existingTable);
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

										return flagService.getActiveFlag(session, originalSystem.getEnterpriseID(), identityToken)
												.map(activeFlag -> {
													newTableForClassification.setActiveFlagID(activeFlag);
													newTableForClassification.setValue(value);
													newTableForClassification.setEnterpriseID(system.getEnterpriseID());
													configureEventLinkValue(newTableForClassification, (J) this, null, classification, value, system.getEnterpriseID());
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
									});
						});
			});
	}

	/**
	 * Updates an event with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> updateEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
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

							return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
									.chain(archivedFlag -> {
										existingTable.setActiveFlagID(archivedFlag);
										existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
										return session.merge(existingTable);
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

										return flagService.getActiveFlag(session, originalSystem.getEnterpriseID(), identityToken)
												.map(activeFlag -> {
													newTableForClassification.setActiveFlagID(activeFlag);
													newTableForClassification.setValue(value);
													newTableForClassification.setEnterpriseID(system.getEnterpriseID());
													configureEventLinkValue(newTableForClassification, (J) this, null, classification, value, system.getEnterpriseID());
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
									});
						});
			});
	}

	/**
	 * Expires an event with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> expireEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
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

									// Otherwise, expire the relation
									existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
									return session.merge(existingTable);
								});
					});
	}

	/**
	 * Archives an event with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> archiveEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
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
									return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
											.chain(archivedFlag -> {
												existingTable.setActiveFlagID(archivedFlag);
												existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
												return session.merge(existingTable);
											});
								});
					});
	}

	/**
	 * Removes an event with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEvent<?, ?>, ?>> removeEvents(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEvent<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
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
									return flagService.getDeletedFlag(session, system.getEnterpriseID(), identityToken)
											.chain(deletedFlag -> {
												existingTable.setActiveFlagID(deletedFlag);
												existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
												return session.merge(existingTable);
											});
								});
					});
	}
}