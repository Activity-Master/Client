package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRules;
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

@SuppressWarnings({"DuplicatedCode", "UnusedReturnValue"})
public interface IManageRules<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
	private String getRulessRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XRules";
		return className;
	}

	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?>> getRulesRelationshipClass()
	{
		String joinTableName = getRulessRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find resourceItem linked class - " + joinTableName, e);
		}
	}

	@SuppressWarnings("rawtypes")
	void configureRulesAddable(IWarehouseRelationshipTable linkTable, J primary, IRules<?,?> secondary, IClassification<?,?> classificationValue, String value, ISystems<?,?> system);

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> findRules(Mutiny.Session session, String classification, String searchValue, ISystems<?,?> system, boolean first, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> relationshipTable = get(getRulesRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IRules<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
				= relationshipTable.builder(session)
				                   .findLink((J) this, null, null)
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
			.onItem().ifNull().failWith(() -> new NoSuchElementException("Rule not found"))
			.map(item -> (IRelationshipValue<J, IRules<?, ?>, ?>) item);
	}

	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IRules<?, ?>, ?>>> findRulesAll(Mutiny.Session session, String classification, String searchValue, ISystems<?,?> system, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> relationshipTable = get(getRulesRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IRules<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
				= relationshipTable.builder(session)
				                   .findLink((J) this, null, null)
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
			.map(list -> (List<IRelationshipValue<J, IRules<?, ?>, ?>>) list);
	}

	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfRuless(Mutiny.Session session, String classificationValue, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> relationshipTable = get(getRulesRelationshipClass());

		// Prepare classification name
		final String finalClassificationValue = classificationValue == null
				? DefaultClassifications.NoClassification.classificationValue()
				: classificationValue;

		return relationshipTable.builder(session)
				.findLink((J) this, null, value)
				.withClassification(finalClassificationValue, system)
				.inActiveRange()
				.inDateRange()
				.canRead(system, identityToken)
				.getCount();
	}


	default Uni<Boolean> hasRuless(Mutiny.Session session, String resourceItemTypeName, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		return numberOfRuless(session, resourceItemTypeName, searchValue, system, identityToken)
				.map(count -> count > 0);
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> addRules(Mutiny.Session session, IRules<?,?> resourceItem,
																 String classificationName,
																 String value,
																 ISystems<?,?> system,
																 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> tableForClassification = get(getRulesRelationshipClass());
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;

			return classificationService.find(session, finalClassificationNameCopy, system, identityToken)
				.map(classification -> {
					tableForClassification.setEnterpriseID(system.getEnterpriseID());
					tableForClassification.setValue(Strings.nullToEmpty(value));
					tableForClassification.setSystemID(system);
					tableForClassification.setOriginalSourceSystemID(system.getId());
					tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
					tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
					tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
					tableForClassification.setActiveFlagID(system.getActiveFlagID());
					tableForClassification.setClassificationID(classification);

					configureRulesAddable(tableForClassification, (J) this,
							resourceItem,
							classification, value, system);

					return tableForClassification;
				})
				.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
				.chain(table -> {
					// Start the createDefaultSecurity operation but don't wait for it to complete
					table.createDefaultSecurity(session, system, identityToken);
					// Return the table immediately without waiting for createDefaultSecurity to complete
					return Uni.createFrom().item((IRelationshipValue<J, IRules<?, ?>, ?>) table);
				});
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> addOrUpdateRules(Mutiny.Session session, String classificationValue,
																		 IRules<?,?> resourceItemType,
																		 String searchValue,
																		 String storeValue,
																		 ISystems<?,?> system,
																		 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> tableForClassification = get(getRulesRelationshipClass());
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationValue) 
					? DefaultClassifications.NoClassification.toString() 
					: classificationValue;

			return classificationService.find(session, finalClassificationName, system, identityToken)
				.chain(classification -> {
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
							return (Uni) addRules(session, resourceItemType, finalClassificationName, storeValue, system, identityToken);
						})
						.chain(result -> {

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IRules<?, ?>, ?>) existingTable);
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
									IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> newTableForClassification = get(getRulesRelationshipClass());
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
											newTableForClassification.setValue(storeValue == null ? "" : storeValue);
											newTableForClassification.setEnterpriseID(system.getEnterpriseID());
											configureRulesAddable(newTableForClassification, existingTable.getPrimary(), existingTable.getSecondary(),
													classification, storeValue, system);
											return newTableForClassification;
										});
								})
								.chain(newTable -> {
									return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
								})
								.chain(newTable -> {
									// Start the createDefaultSecurity operation but don't wait for it to complete
									newTable.createDefaultSecurity(session, originalSystem, identityToken);
									// Return the table immediately without waiting for createDefaultSecurity to complete
									return Uni.createFrom().item((IRelationshipValue<J, IRules<?, ?>, ?>) newTable);
								});
						});
				});
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> addOrReuseRules(Mutiny.Session session, String classificationValue,
																		IRules<?,?> resourceItemType,
																		String searchValue,
																		ISystems<?,?> system,
																		UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> tableForClassification = get(getRulesRelationshipClass());

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationValue) 
					? DefaultClassifications.NoClassification.toString() 
					: classificationValue;

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
					return (Uni) addRules(session, resourceItemType, finalClassificationName, searchValue, system, identityToken);
				})
				.chain(result -> {

					// Cast the result to the correct type and return it
					return Uni.createFrom().item((IRelationshipValue<J, IRules<?, ?>, ?>) result);
				});
	}

}

