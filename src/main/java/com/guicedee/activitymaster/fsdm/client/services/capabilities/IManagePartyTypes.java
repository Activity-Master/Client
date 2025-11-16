package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"DuplicatedCode", "unused"})
public interface IManagePartyTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
	private String getInvolvedPartyTypesRelationshipTable()
	{
		return getClass().getCanonicalName() + "XInvolvedPartyType";
	}

	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?>> getInvolvedPartyTypeRelationshipClass()
	{
		String joinTableName = getInvolvedPartyTypesRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find involvedPartyType linked class - " + joinTableName, e);
		}
	}

	/**
	 * Configures an involved party type.
	 * 
	 * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
	 * It doesn't need to return a Uni as it's a synchronous operation.
	 */
	@SuppressWarnings("rawtypes")
	void configureInvolvedPartyTypeAddable(IWarehouseRelationshipTable linkTable, J primary, IInvolvedPartyType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, ISystems<?, ?> system);

	/**
	 * Finds an involved party type with the given classification, type, search value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>> findInvolvedPartyType(Mutiny.Session session, String classification, String ipType, String searchValue, ISystems<?, ?> system,
																						  boolean first, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> relationshipTable = get(getInvolvedPartyTypeRelationshipClass());
		IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

		return partyService.findType(session, ipType, system, identityToken)
			.chain(type -> {
				IQueryBuilderRelationships<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
						= relationshipTable.builder(session)
						                   .findLink((J) this, type, null)
						                   .inActiveRange()
						                   .withClassification(classification, system)
						                   .withValue(searchValue)
						                   .inDateRange()
						                   .withEnterprise(system.getEnterprise())
						                   .canRead(system, identityToken);
				if (first)
				{
					queryBuilderRelationshipClassification.setMaxResults(1);
				}
				if (latest)
				{
					queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
				}

				return queryBuilderRelationshipClassification.get()
					.onItem().ifNull().failWith(() -> new NoSuchElementException("Involved party type not found"))
					.map(item -> (IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>) item);
			});
	}

	/**
	 * Finds all involved party types with the given classification, type, search value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>>> findInvolvedPartyTypesAll(Mutiny.Session session, String classification, String ipType, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> relationshipTable = get(getInvolvedPartyTypeRelationshipClass());
		IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

		return partyService.findType(session, ipType, system, identityToken)
			.chain(type -> {
				IQueryBuilderRelationships<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
						= relationshipTable.builder(session)
						                   .findLink((J) this, type, null)
						                   .inActiveRange()
						                   .withClassification(classification, system)
						                   .withValue(searchValue)
						                   .inDateRange()
						                   .withEnterprise(system.getEnterprise())
						                   .canRead(system, identityToken);
				if (latest)
				{
					queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
				}

				return queryBuilderRelationshipClassification.getAll()
					.map(list -> (List<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>>) list);
			});
	}

	/**
	 * Gets the number of involved party types with the given classification value, type, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfInvolvedPartyTypes(Mutiny.Session session, String classificationValue, String ipType, String value, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> relationshipTable = get(getInvolvedPartyTypeRelationshipClass());
		IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

		if (classificationValue == null)
		{
			classificationValue = DefaultClassifications.NoClassification.classificationValue();
		}

		final String finalClassificationValue = classificationValue;

		return partyService.findType(session, ipType, system, identityToken)
			.chain(type -> relationshipTable.builder(session)
				.findLink((J) this, type, null)
				.withValue(value)
				.withClassification(finalClassificationValue, system)
				.inActiveRange()
				.inDateRange()
				.canRead(system, identityToken)
				.getCount());
	}

	/**
	 * Checks if the entity has involved party types with the given classification name, type, search value, and system.
	 */
	default Uni<Boolean> hasInvolvedPartyTypes(Mutiny.Session session, String classificationName, String ipType, String searchValue, ISystems<?, ?> system, UUID... identityToken)
	{
		return numberOfInvolvedPartyTypes(session, classificationName, ipType, searchValue, system, identityToken)
			.map(count -> count > 0);
	}

	/**
	 * Adds an involved party type with the given type, classification name, value, and system.
	 */
	default Uni<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>> addInvolvedPartyType(Mutiny.Session session, String involvedPartyIdentificationType,
																						 String classificationName,
																						 String value,
																						 ISystems<?, ?> system,
																						 UUID... identityToken)
	{
		@SuppressWarnings("unchecked")
		IInvolvedPartyService<?> partyService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
		return partyService.findType(session, involvedPartyIdentificationType, system, identityToken)
			.chain(type -> addInvolvedPartyType(session, type, classificationName, value, system, identityToken));
	}

	/**
	 * Adds an involved party type with the given type, classification name, value, and system.
	 */
	default Uni<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>> addInvolvedPartyType(Mutiny.Session session, IInvolvedPartyType<?, ?> involvedPartyIdentificationType,
																						 String classificationName,
																						 String value,
																						 ISystems<?, ?> system,
																						 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyTypeRelationshipClass());
			IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);

			return classificationService.find(session, classificationName, system, identityToken)
				.map(classification -> {
					tableForClassification.setEnterpriseID(system.getEnterpriseID());
					tableForClassification.setValue(Strings.nullToEmpty(value));
					tableForClassification.setSystemID(system);
					tableForClassification.setOriginalSourceSystemID(system.getId());
					tableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
					tableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
					tableForClassification.setActiveFlagID(system.getActiveFlagID());
					tableForClassification.setClassificationID(classification);

					configureInvolvedPartyTypeAddable(tableForClassification, (J) this,
							involvedPartyIdentificationType,
							classification, value, system);

					return tableForClassification;
				})
				.chain(table -> session.merge(table))
				.chain(table -> {
					// Start the createDefaultSecurity operation but don't wait for it to complete
					table.createDefaultSecurity(session, system, identityToken);
					// Return the table immediately without waiting for createDefaultSecurity to complete
					return Uni.createFrom().item((IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>) table);
				});
	}

	/**
	 * Adds or updates an involved party type with the given classification value, type, search value, store value, and system.
	 */
	default Uni<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>> addOrUpdateInvolvedPartyType(Mutiny.Session session, String classificationValue,
																								 String involvedPartyType,
																								 String searchValue,
																								 String storeValue,
																								 ISystems<?, ?> system,
																								 UUID... identityToken)
	{
		@SuppressWarnings("unchecked")
		IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
		return partyService.findType(session, involvedPartyType, system, identityToken)
			.chain(type -> addOrUpdateInvolvedPartyType(session, classificationValue, type, searchValue, storeValue, system, identityToken));
	}

	/**
	 * Adds or updates an involved party type with the given classification value, type, search value, store value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>> addOrUpdateInvolvedPartyType(Mutiny.Session session, String classificationValue,
																								 IInvolvedPartyType<?, ?> involvedPartyType,
																								 String searchValue,
																								 String storeValue,
																								 ISystems<?, ?> system,
																								 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyTypeRelationshipClass());
			IClassificationService<?> classificationService = get(IClassificationService.class);

			return classificationService.find(session, classificationValue, system, identityToken)
				.chain(classification -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, involvedPartyType, null)
						.withValue(searchValue)
						.inActiveRange()
						.inDateRange()
						.withClassification(classificationValue, system)
						//.canCreate(system.getEnterpriseID(), identityToken)
						.get()
						.onFailure(NoResultException.class)
						.recoverWithUni(() -> {
							return (Uni) addInvolvedPartyType(session, involvedPartyType, classificationValue, storeValue, system, identityToken);
						})
						.chain(result -> {
							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>) existingTable);
							}

							// Otherwise, update the relation
							IActiveFlagService<?> flagService = get(IActiveFlagService.class);

							return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
								.chain(archivedFlag -> {
									existingTable.setActiveFlagID(archivedFlag);
									existingTable.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									return session.merge(existingTable);
								})
								.chain(() -> {
									IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getInvolvedPartyTypeRelationshipClass());
									newTableForClassification.setId(null);
									newTableForClassification.setClassificationID(existingTable.getClassificationID());
									newTableForClassification.setSystemID(system);
									newTableForClassification.setOriginalSourceSystemID(existingTable.getId());
									newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
									newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));

									return flagService.getActiveFlag(session, system.getEnterpriseID(), identityToken)
										.map(activeFlag -> {
											newTableForClassification.setActiveFlagID(activeFlag);
											newTableForClassification.setValue(storeValue == null ? "" : storeValue);
											newTableForClassification.setEnterpriseID(system.getEnterpriseID());
											configureInvolvedPartyTypeAddable(newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(),
													classification, storeValue, system);
											return newTableForClassification;
										});
								})
								.chain(newTable -> {
									return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
								})
								.chain(newTable -> {
									// Start the createDefaultSecurity operation but don't wait for it to complete
									newTable.createDefaultSecurity(session, system, identityToken);
									// Return the table immediately without waiting for createDefaultSecurity to complete
									return Uni.createFrom().item((IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>) newTable);
								});
						});
				});
	}

	/**
	 * Adds or reuses an involved party type with the given classification value, type, search value, and system.
	 */
	default Uni<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>> addOrReuseInvolvedPartyType(Mutiny.Session session, String classificationValue,
																								String involvedPartyType,
																								String searchValue,
																								ISystems<?, ?> system,
																								UUID... identityToken)
	{
		IInvolvedPartyService<?> service = get(IInvolvedPartyService.class);
		return service.findType(session, involvedPartyType, system, identityToken)
			.chain(type -> addOrReuseInvolvedPartyType(session, classificationValue, type, searchValue, system, identityToken));
	}

	/**
	 * Adds or reuses an involved party type with the given classification value, type, search value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>> addOrReuseInvolvedPartyType(Mutiny.Session session, String classificationValue,
																								IInvolvedPartyType<?, ?> involvedPartyType,
																								String searchValue,
																								ISystems<?, ?> system,
																								UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyTypeRelationshipClass());

			// Create a query to find the existing relationship
			return tableForClassification.builder(session)
				.findLink((J) this, involvedPartyType, null)
				.withValue(searchValue)
				.inActiveRange()
				.inDateRange()
				.withClassification(classificationValue, system)
				//.canCreate(system.getEnterpriseID(), identityToken)
				.get()
				.onFailure(NoResultException.class)
				.recoverWithUni(() -> {
					return (Uni) addInvolvedPartyType(session, involvedPartyType, classificationValue, searchValue, system, identityToken);
				})
				.chain(result -> {
					// Otherwise, return the existing relation
					return Uni.createFrom().item((IRelationshipValue<J, IInvolvedPartyType<?, ?>, ?>) result);
				});
	}
}

