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
public interface IManageEventTypes<J extends IWarehouseBaseTable<J, ?,? extends Serializable>>
{
	private String getEventTypeRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XEventType";
		return className;
	}
	
	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>> getEventTypeRelationshipClass()
	{
		String joinTableName = getEventTypeRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find eventItemType linked class - " + joinTableName, e);
		}
	}
	
	/**
	 * Finds an event type with the given classification name, event type, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> findEventType(Mutiny.Session session, String classificationName, String eventType, String value, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
		IEventService<?> eventService = get(IEventService.class);
		
		return eventService.findEventType(session, eventType, system, identityToken)
				.chain(eventItemType -> {
					IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
							.findLink((J) this, eventItemType, value)
							.inActiveRange()
							.withClassification(classificationName, system)
							.inDateRange()
							.canRead(system, identityToken);
					
					return query.get()
							.onItem().ifNull().failWith(() -> new NoSuchElementException("Event type not found"))
							.map(item -> {
								// Explicit cast to handle type compatibility
								return (IRelationshipValue<J, IEventType<?, ?>, ?>) item;
							});
				});
	}
	
	/**
	 * Finds all event types with the given classification name and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IEventType<?, ?>, ?>>> findEventTypesAll(Mutiny.Session session, String classificationName, ISystems<?, ?> system, UUID... identityToken)
	{
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
	
	/**
	 * Checks if the entity has event types with the given classification name, event item type name, and system.
	 */
	default Uni<Boolean> hasEventTypes(Mutiny.Session session, String classificationName, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		return numberOfEventTypes(session, classificationName, eventItemTypeName, system, identityToken)
				.map(count -> count > 0);
	}
	
	/**
	 * Gets the number of event types with the given classification name, event item type name, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfEventTypes(Mutiny.Session session, String classificationName, String eventItemTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
		IEventService<?> eventService = get(IEventService.class);
		
		return eventService.findEventType(session, eventItemTypeName, system, identityToken)
				.chain(eventType -> {
					IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
							.findLink((J) this, eventType, null)
							.inActiveRange()
							.withClassification(classificationName, system)
							.inDateRange()
							.canRead(system, identityToken);
					
					return query.getCount();
				});
	}
	
	/**
	 * Adds an event type with the given event type, value, classification name, and system.
	 */
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addEventTypes(Mutiny.Session session, Enum<?> eventType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken)
	{
		return addEventTypes(session, eventType.toString(), value, classificationName, system, identityToken);
	}
	
	/**
	 * Adds an event type with the given event type, value, classification name, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addEventTypes(Mutiny.Session session, String eventType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);
			
			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}
			
			final String finalClassificationNameCopy = finalClassificationName;
			
			// First get the event type
			return eventService.findEventType(session, eventType, system, identityToken)
					.chain(eventItemType -> {
						// Then get the classification
						return classificationService.find(session, finalClassificationNameCopy, system, identityToken)
								.map(classification -> {
									// Set up the table
									tableForClassification.setEnterpriseID(system.getEnterpriseID());
									tableForClassification.setValue(value);
									tableForClassification.setSystemID(system);
									tableForClassification.setClassificationID(classification);
									tableForClassification.setOriginalSourceSystemID(system.getId());
									tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
									tableForClassification.setActiveFlagID(system.getActiveFlagID());
									configureEventTypeLinkValue(tableForClassification, (J) this, eventItemType, classification, value, system.getEnterpriseID());
									
									return tableForClassification;
								})
								.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
								.chain(table -> {
									// Chain the security setup operation
									return table.createDefaultSecurity(session, system, identityToken)
										.onFailure().recoverWithNull()  // Continue even if security setup fails
										.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) table));
								});
					});
	}
	
	/**
	 * Configures an event type link value.
	 * 
	 * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
	 * It doesn't need to return a Uni as it's a synchronous operation.
	 */
	@SuppressWarnings("rawtypes")
	void configureEventTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IEventType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);
	
	/**
	 * Adds or reuses an event type with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addOrReuseEventTypes(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			
			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName) 
					? DefaultClassifications.NoClassification.toString() 
					: classificationName;
			
			// First get the event type
			return eventService.findEventType(session, eventTypeName, system, identityToken)
					.chain(eventItemType -> {
						// Create a query to find the existing relationship
						IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
								.findLink((J) this, eventItemType, searchValue)
								.inActiveRange()
								.withClassification(finalClassificationName, system)
								.inDateRange()
								.canRead(system, identityToken);
						
						// Get the result and handle it
						return query.get()
								.onFailure(NoResultException.class)
								.recoverWithUni(() -> {
									return (Uni) addEventTypes(session, eventTypeName, value, finalClassificationName, system, identityToken);
								})
								.chain(result -> {
									// Cast the result to the correct type and return it
									return Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) result);
								});
					});
	}
	
	/**
	 * Adds or updates an event type with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> addOrUpdateEventTypes(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);
			
			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}
			
			final String finalClassificationNameCopy = finalClassificationName;
			
			// First get the event type
			return eventService.findEventType(session, eventTypeName, system, identityToken)
					.chain(eventItemType -> {
						// Then get the classification
						return classificationService.find(session, finalClassificationNameCopy, system, identityToken)
								.chain(classification -> {
									// Create a query to find the existing relationship
									IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
											.findLink((J) this, eventItemType, searchValue)
											.inActiveRange()
											.withClassification(finalClassificationNameCopy, system)
											.inDateRange()
											.canRead(system, identityToken);
									
									// Get the result and handle it
									return query.get()
											.onFailure(NoResultException.class)
											.recoverWithUni(() -> {
													tableForClassification.setEnterpriseID(system.getEnterpriseID());
													tableForClassification.setValue(value);
													tableForClassification.setSystemID(system);
													tableForClassification.setOriginalSourceSystemID(system.getId());
													tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
													tableForClassification.setActiveFlagID(system.getActiveFlagID());
													configureEventTypeLinkValue(tableForClassification, (J) this, eventItemType, classification, value, system.getEnterpriseID());
													
													return (Uni) Uni.createFrom().item(tableForClassification)
															.chain(table -> {
																return session.persist(table).replaceWith(Uni.createFrom().item(table));
															})
															.chain(table -> {
																// Chain the security setup operation
																return table.createDefaultSecurity(session, system, identityToken)
																	.onFailure().recoverWithNull()  // Continue even if security setup fails
																	.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) table));
															});
											})
											.chain(result -> {
												
												// Cast the result to the correct type
												IRelationshipValue<J, IEventType<?, ?>, ?> existingRelation = (IRelationshipValue<J, IEventType<?, ?>, ?>) result;
												
												// If the value is the same, return the existing relation
												if (Strings.nullToEmpty(value).equals(existingRelation.getValue())) {
													return Uni.createFrom().item(existingRelation);
												}
												
												// Otherwise, update the relation
												final IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) result;
												ISystems<?, ?> originalSystem = existingTable.getSystemID();
												IActiveFlagService<?> flagService = get(IActiveFlagService.class);
												
												return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
														.chain(archivedFlag -> {
															existingTable.setActiveFlagID(archivedFlag);
															existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
															return session.merge(existingTable);
														})
														.chain(() -> {
															IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getEventTypeRelationshipClass());
															newTableForClassification.setId(null);
															newTableForClassification.setClassificationID(existingTable.getClassificationID());
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
																		configureEventTypeLinkValue(newTableForClassification, (J) this, eventItemType, classification, value, system.getEnterpriseID());
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
																.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) newTable));
														});
											});
								});
					});
	}
	
	/**
	 * Updates an event type with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> updateEventTypes(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);
			
			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}
			
			final String finalClassificationNameCopy = finalClassificationName;
			
			// First get the event type
			return eventService.findEventType(session, eventTypeName, system, identityToken)
					.chain(eventItemType -> {
						// Then get the classification
						return classificationService.find(session, finalClassificationNameCopy, system, identityToken)
								.chain(classification -> {
									// Create a query to find the existing relationship
									IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
											.findLink((J) this, eventItemType, searchValue)
											.inActiveRange()
											.withClassification(finalClassificationNameCopy, system)
											.inDateRange()
											.canRead(system, identityToken);
									
									// Get the result and handle it
									return query.get()
											.chain(result -> {
												// If result is null, do nothing
												if (result == null) {
													return Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) tableForClassification);
												}
												
												// Cast the result to the correct type
												IRelationshipValue<J, IEventType<?, ?>, ?> existingRelation = (IRelationshipValue<J, IEventType<?, ?>, ?>) result;
												
												// If the value is the same, return the existing relation
												if (Strings.nullToEmpty(value).equals(existingRelation.getValue())) {
													return Uni.createFrom().item(existingRelation);
												}
												
												// Otherwise, update the relation
												final IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) result;
												ISystems<?, ?> originalSystem = existingTable.getSystemID();
												IActiveFlagService<?> flagService = get(IActiveFlagService.class);
												
												return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
														.chain(archivedFlag -> {
															existingTable.setActiveFlagID(archivedFlag);
															existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
															return session.merge(existingTable);
														})
														.chain(() -> {
															IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getEventTypeRelationshipClass());
															newTableForClassification.setId(null);
															newTableForClassification.setClassificationID(existingTable.getClassificationID());
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
																		configureEventTypeLinkValue(newTableForClassification, (J) this, eventItemType, classification, value, system.getEnterpriseID());
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
																.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) newTable));
														});
											});
								});
					});
	}
	
	/**
	 * Expires an event type with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> expireEventTypes(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			
			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}
			
			final String finalClassificationNameCopy = finalClassificationName;
			
			// First get the event type
			return eventService.findEventType(session, eventTypeName, system, identityToken)
					.chain(eventItemType -> {
						// Create a query to find the existing relationship
						IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
								.findLink((J) this, eventItemType, searchValue)
								.inActiveRange()
								.withClassification(finalClassificationNameCopy, system)
								.inDateRange()
								.canRead(system, identityToken);
						
						// Get the result and handle it
						return query.get()
								.chain(result -> {
									// If result is null, do nothing
									if (result == null) {
										return Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) tableForClassification);
									}
									
									// Cast the result to the correct type
									IRelationshipValue<J, IEventType<?, ?>, ?> existingRelation = (IRelationshipValue<J, IEventType<?, ?>, ?>) result;
									
									// If the value is the same, return the existing relation
									if (Strings.nullToEmpty(value).equals(existingRelation.getValue())) {
										return Uni.createFrom().item(existingRelation);
									}
									
									// Otherwise, expire the relation
									final IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) result;
									existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
									return session.merge(existingTable);
								});
					});
	}
	
	/**
	 * Archives an event type with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> archiveEventTypes(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			
			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}
			
			final String finalClassificationNameCopy = finalClassificationName;
			
			// First get the event type
			return eventService.findEventType(session, eventTypeName, system, identityToken)
					.chain(eventItemType -> {
						// Create a query to find the existing relationship
						IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
								.findLink((J) this, eventItemType, searchValue)
								.inActiveRange()
								.withClassification(finalClassificationNameCopy, system)
								.inDateRange()
								.canRead(system, identityToken);
						
						// Get the result and handle it
						return query.get()
								.chain(result -> {
									// If result is null, do nothing
									if (result == null) {
										return Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) tableForClassification);
									}
									
									// Cast the result to the correct type
									IRelationshipValue<J, IEventType<?, ?>, ?> existingRelation = (IRelationshipValue<J, IEventType<?, ?>, ?>) result;
									
									// If the value is the same, return the existing relation
									if (Strings.nullToEmpty(value).equals(existingRelation.getValue())) {
										return Uni.createFrom().item(existingRelation);
									}
									
									// Otherwise, archive the relation
									final IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) result;
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
	 * Removes an event type with the given event type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IEventType<?, ?>, ?>> removeEventTypes(Mutiny.Session session, String eventTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> tableForClassification = get(getEventTypeRelationshipClass());
			IEventService<?> eventService = get(IEventService.class);
			
			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}
			
			final String finalClassificationNameCopy = finalClassificationName;
			
			// First get the event type
			return eventService.findEventType(session, eventTypeName, system, identityToken)
					.chain(eventItemType -> {
						// Create a query to find the existing relationship
						IQueryBuilderRelationships<?, ?, J, IEventType<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
								.findLink((J) this, eventItemType, searchValue)
								.inActiveRange()
								.withClassification(finalClassificationNameCopy, system)
								.inDateRange()
								.canRead(system, identityToken);
						
						// Get the result and handle it
						return query.get()
								.chain(result -> {
									// If result is null, do nothing
									if (result == null) {
										return Uni.createFrom().item((IRelationshipValue<J, IEventType<?, ?>, ?>) tableForClassification);
									}
									
									// Cast the result to the correct type
									IRelationshipValue<J, IEventType<?, ?>, ?> existingRelation = (IRelationshipValue<J, IEventType<?, ?>, ?>) result;
									
									// If the value is the same, return the existing relation
									if (Strings.nullToEmpty(value).equals(existingRelation.getValue())) {
										return Uni.createFrom().item(existingRelation);
									}
									
									// Otherwise, remove the relation
									final IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IEventType<?, ?>, java.util.UUID, ?>) result;
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