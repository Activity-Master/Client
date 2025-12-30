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
public interface IManageArrangementTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
		private String getArrangementTypesRelationshipTable()
		{
				String className = getClass().getCanonicalName() + "XArrangementType";
				return className;
		}
		
		private Class<? extends IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?>> getArrangementTypeRelationshipClass()
		{
				String joinTableName = getArrangementTypesRelationshipTable();
				try
				{
						//noinspection unchecked
						return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
				}
				catch (ClassNotFoundException e)
				{
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
		
		/**
			* Finds an arrangement type with the given classification, arrangement type name, search value, and system.
			*/
		@SuppressWarnings("unchecked")
		default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> findArrangementType(Mutiny.Session session, String classification, String arrangementTypeName, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
		{
				IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementTypeRelationshipClass());
				IArrangementsService<?> partyService = get(IArrangementsService.class);
				
				return partyService
												.findArrangementType(session, arrangementTypeName, system, identityToken)
												.chain(arrangementType -> {
														IQueryBuilderRelationships<?, ?, J, IArrangementType<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
															= relationshipTable
																		.builder(session)
																		.findLink((J) this, arrangementType, null)
																		.inActiveRange()
																		.withClassification(classification, system)
																		.withValue(searchValue)
																		.inDateRange()
																		.withEnterprise(system.getEnterprise())
																		.canRead(system, identityToken)
															;
														if (first)
														{
																queryBuilderRelationshipClassification.setMaxResults(1);
														}
														if (latest)
														{
																queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
														}
														
														// Use a different approach to handle the type compatibility issue
														return queryBuilderRelationshipClassification
																						.get()
																						.onItem()
																						.ifNull()
																						.failWith(() -> new NoSuchElementException("Arrangement type not found"))
																						.map(item -> {
																								// Explicit cast to handle type compatibility
																								return (IRelationshipValue<J, IArrangementType<?, ?>, ?>) item;
																						});
												});
		}
		
		/**
			* Finds all arrangement types with the given classification, arrangement type name, search value, and system.
			*/
		@SuppressWarnings("unchecked")
		default Uni<List<IRelationshipValue<J, IArrangementType<?, ?>, ?>>> findArrangementTypesAll(Mutiny.Session session, String classification, String arrangementTypeName, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
		{
				IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementTypeRelationshipClass());
				IArrangementsService<?> partyService = get(IArrangementsService.class);
				
				return partyService
												.findArrangementType(session, arrangementTypeName, system, identityToken)
												.chain(arrangementType -> {
														IQueryBuilderRelationships<?, ?, J, IArrangementType<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
															= relationshipTable
																		.builder(session)
																		.findLink((J) this, arrangementType, null)
																		.inActiveRange()
																		.withClassification(classification, system)
																		.withValue(searchValue)
																		.inDateRange()
																		.withEnterprise(system.getEnterprise())
																		.canRead(system, identityToken)
															;
														if (latest)
														{
																queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
														}
														
														// Use a different approach to handle the type compatibility issue
														return queryBuilderRelationshipClassification
																						.getAll()
																						.map(list -> {
																								// Explicit cast to handle type compatibility
																								return (List<IRelationshipValue<J, IArrangementType<?, ?>, ?>>) list;
																						});
												});
		}
		
		/**
			* Gets the number of arrangement types with the given classification value, arrangement type name, value, and system.
			*/
		@SuppressWarnings("unchecked")
		default Uni<Long> numberOfArrangementTypes(Mutiny.Session session, String classificationValue, String arrangementTypeName, String value, ISystems<?, ?> system, UUID... identityToken)
		{
				IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementTypeRelationshipClass());
				if (classificationValue == null)
				{
						classificationValue = DefaultClassifications.NoClassification.classificationValue();
				}
				
				IArrangementsService<?> partyService = get(IArrangementsService.class);
				final String finalClassificationValue = classificationValue;
				
				return partyService
												.findArrangementType(session, arrangementTypeName, system, identityToken)
												.chain(arrangementType -> relationshipTable
																																							.builder(session)
																																							.findLink((J) this, null, value)
																																							.withClassification(finalClassificationValue, system)
																																							.inActiveRange()
																																							.inDateRange()
																																							.canRead(system, identityToken)
																																							.getCount());
		}
		
		/**
			* Checks if the entity has arrangement types with the given classification name, arrangement type type name, search value, and system.
			*/
		default Uni<Boolean> hasArrangementTypes(Mutiny.Session session, String classificationName, String arrangementTypeTypeName, String searchValue, ISystems<?, ?> system, UUID... identityToken)
		{
				return numberOfArrangementTypes(session, classificationName, arrangementTypeTypeName, searchValue, system, identityToken)
												.map(count -> count > 0);
		}
		
		/**
			* Adds an arrangement type with the given arrangement type, classification name, value, and system.
			*/
		default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> addArrangementType(Mutiny.Session session, IArrangementType<?, ?> arrangementType,
																																																																																			String classificationName,
																																																																																			String value,
																																																																																			ISystems<?, ?> system,
																																																																																			UUID... identityToken)
		{
				IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementTypeRelationshipClass());
				IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
				
				return classificationService
												.find(session, classificationName, system, identityToken)
												.map(classification -> {
														tableForClassification.setEnterpriseID(system.getEnterpriseID());
														tableForClassification.setValue(Strings.nullToEmpty(value));
														tableForClassification.setSystemID(system);
														tableForClassification.setOriginalSourceSystemID(system.getId());
														tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
														tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
														tableForClassification.setActiveFlagID(system.getActiveFlagID());
														tableForClassification.setClassificationID(classification);
														
														configureArrangementTypeAddable(tableForClassification, (J) this,
																																														arrangementType,
																																														classification, value, system);
														
														return tableForClassification;
												})
												.chain(table -> session
																													.persist(table)
																													.replaceWith(Uni
																																											.createFrom()
																																											.item(table)))
												.chain(table -> {
														// Start the createDefaultSecurity operation but don't wait for it to complete
														table.createDefaultSecurity(session, system, identityToken);
														// Return the table immediately without waiting for createDefaultSecurity to complete
														return Uni
																						.createFrom()
																						.item((IRelationshipValue<J, IArrangementType<?, ?>, ?>) table);
												});
		}
		
		/**
			* Adds or updates an arrangement type with the given classification value, arrangement type type, search value, store value, and system.
			*/
		@SuppressWarnings("unchecked")
		default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> addOrUpdateArrangementType(Mutiny.Session session, String classificationValue,
																																																																																											IArrangementType<?, ?> arrangementTypeType,
																																																																																											String searchValue,
																																																																																											String storeValue,
																																																																																											ISystems<?, ?> system,
																																																																																											UUID... identityToken)
		{
				final IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementTypeRelationshipClass());
				IClassificationService<?> classificationService = get(IClassificationService.class);
				
				return classificationService
												.find(session, classificationValue, system, identityToken)
												.chain(classification -> {
														// Create a query to find the existing relationship
														IQueryBuilderRelationships<?, ?, J, IArrangementType<?, ?>, java.util.UUID> query = tableForClassification
																																																																																																			.builder(session)
																																																																																																			.findLink((J) this, arrangementTypeType, null)
																																																																																																			.withValue(searchValue)
																																																																																																			.inActiveRange()
																																																																																																			.inDateRange()
																																																																																																			.withClassification(classificationValue, system)
															;
														
														// Get the result and handle it
														return query
																						.get()
																						.onFailure(NoResultException.class)
																						.recoverWithUni(() -> {
																								return (Uni) addArrangementType(session, arrangementTypeType, classificationValue, storeValue, system, identityToken);
																						})
																						.chain(result -> {
																								// Cast the result to the correct type
																								IRelationshipValue<J, IArrangementType<?, ?>, ?> existingRelation = (IRelationshipValue<J, IArrangementType<?, ?>, ?>) result;
																								
																								// If the value is the same, return the existing relation
																								if (Strings
																													.nullToEmpty(storeValue)
																													.equals(existingRelation.getValue()))
																								{
																										return Uni
																																		.createFrom()
																																		.item(existingRelation);
																								}
																								
																								// Otherwise, update the relation
																								final IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?>) result;
																								IActiveFlagService<?> flagService = get(IActiveFlagService.class);
																								
																								return flagService
																																.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
																																.chain(archivedFlag -> {
																																		existingTable.setActiveFlagID(archivedFlag);
																																		existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
																																		return session.merge(existingTable);
																																})
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
																																		
																																		return flagService
																																										.getActiveFlag(session, system.getEnterpriseID(), identityToken)
																																										.map(activeFlag -> {
																																												newTableForClassification.setActiveFlagID(activeFlag);
																																												newTableForClassification.setValue(storeValue == null ? "" : storeValue);
																																												newTableForClassification.setEnterpriseID(system.getEnterpriseID());
																																												configureArrangementTypeAddable(newTableForClassification, existingTable.getPrimary(), existingTable.getSecondary(),
																																																																												classification, storeValue, system);
																																												return newTableForClassification;
																																										});
																																})
																																.chain(newTable -> {
																																		return session
																																										.persist(newTable)
																																										.replaceWith(Uni
																																																								.createFrom()
																																																								.item(newTable));
																																})
																																.chain(newTable -> {
																																		newTable.createDefaultSecurity(session, system, identityToken);
																																		return Uni
																																										.createFrom()
																																										.item((IRelationshipValue<J, IArrangementType<?, ?>, ?>) existingTable);
																																});
																						});
												});
		}
		
		/**
			* Adds or reuses an arrangement type with the given classification value, arrangement type type, search value, and system.
			*/
		@SuppressWarnings("unchecked")
		default Uni<IRelationshipValue<J, IArrangementType<?, ?>, ?>> addOrReuseArrangementType(Mutiny.Session session, String classificationValue,
																																																																																										IArrangementType<?, ?> arrangementTypeType,
																																																																																										String searchValue,
																																																																																										ISystems<?, ?> system,
																																																																																										UUID... identityToken)
		{
				IWarehouseRelationshipTable<?, ?, J, IArrangementType<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementTypeRelationshipClass());
				
				// Create a query to find the existing relationship
				IQueryBuilderRelationships<?, ?, J, IArrangementType<?, ?>, java.util.UUID> query = tableForClassification
																																																																																									.builder(session)
																																																																																									.findLink((J) this, arrangementTypeType, null)
																																																																																									.withValue(searchValue)
																																																																																									.inActiveRange()
																																																																																									.inDateRange()
																																																																																									.withClassification(classificationValue, system)
					;
				
				// Get the result and handle it
				return query
												.get()
												.onFailure(NoResultException.class)
												.recoverWithUni(() -> {
														return (Uni) addArrangementType(session, arrangementTypeType, classificationValue, searchValue, system, identityToken);
												})
												.chain(result -> {
														// Cast the result to the correct type and return it
														return Uni
																						.createFrom()
																						.item((IRelationshipValue<J, IArrangementType<?, ?>, ?>) result);
												});
		}
}
