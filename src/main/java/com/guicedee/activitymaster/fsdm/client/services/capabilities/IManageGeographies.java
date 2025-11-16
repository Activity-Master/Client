package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.geography.IGeography;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

public interface IManageGeographies <J extends IWarehouseBaseTable<J, ?,? extends Serializable>>
{
	private String getGeographysRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XGeography";
		return className;
	}

	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID,?>> getGeographyRelationshipClass()
	{
		String joinTableName = getGeographysRelationshipTable();
		try
		{
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID,?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find geography linked class - " + joinTableName, e);
		}
	}

	void configureGeographyAddable(IWarehouseRelationshipTable linkTable, J primary, IGeography<?,?> secondary, IClassification<?,?> classificationValue, String value, ISystems<?,?> system);

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> findGeography(Mutiny.Session session, String classification, String searchValue, ISystems<?,?> system, boolean first, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID,?> relationshipTable = get(getGeographyRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IGeography<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
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
		//noinspection rawtypes
		return (Uni<IRelationshipValue<J, IGeography<?, ?>, ?>>) queryBuilderRelationshipClassification.get()
				.onItem().ifNull().failWith(() -> new NoSuchElementException("Geography not found"));
	}

	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IGeography<?, ?>, ?>>> findGeographysAll(Mutiny.Session session, String classification, String searchValue, ISystems<?,?> system, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> relationshipTable = get(getGeographyRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IGeography<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
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
		//noinspection rawtypes
		return queryBuilderRelationshipClassification.getAll()
				.map(list -> (List<IRelationshipValue<J, IGeography<?, ?>, ?>>) list);
	}

	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfGeography(Mutiny.Session session, String classificationValue, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> relationshipTable = get(getGeographyRelationshipClass());
		if (classificationValue == null)
		{
			classificationValue = DefaultClassifications.NoClassification.classificationValue();
		}
		return relationshipTable.builder(session)
		                        .findLink((J) this, null, value)
		                        .withClassification(classificationValue, system)
		                        .inActiveRange()
		                        .inDateRange()
		                        .canRead(system, identityToken)
		                        .getCount();
	}


	default Uni<Boolean> hasGeography(Mutiny.Session session, String geographyTypeName, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		return numberOfGeography(session, geographyTypeName,searchValue, system, identityToken)
				.map(count -> count > 0);
	}

	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> addGeography(Mutiny.Session session, IGeography<?,?> geography,
																		 String classificationName,
																		 String value,
																		 ISystems<?,?> system,
																		 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> tableForClassification = get(getGeographyRelationshipClass());
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

					configureGeographyAddable(tableForClassification, (J) this,
							geography,
							classification, value, system);

					return tableForClassification;
				})
				.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
				.chain(table -> {
					// Start the createDefaultSecurity operation but don't wait for it to complete
					table.createDefaultSecurity(session, system, identityToken);
					// Return the table immediately without waiting for createDefaultSecurity to complete
					return Uni.createFrom().item(table);
				});
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> addOrUpdateGeography(Mutiny.Session session, String classificationValue,
																				 IGeography<?,?> geographyType,
																				 String searchValue,
																				 String storeValue,
																				 ISystems<?,?> system,
																				 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> tableForClassification = get(getGeographyRelationshipClass());
			IClassificationService<?> classificationService = get(IClassificationService.class);

			return classificationService.find(session, classificationValue, system, identityToken)
				.chain(classification -> {
					return tableForClassification.builder(session)
						.findLink((J) this, null, null)
						.withValue(searchValue)
						.inActiveRange()
						.inDateRange()
						.withClassification(classificationValue, system)
						//.canCreate(system.getEnterpriseID(), identityToken)
						.get()
						.chain(existingTable -> {
							if (existingTable == null) {
								return addGeography(session, geographyType, classificationValue, storeValue, system, identityToken);
							}

							if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
								return Uni.createFrom().item(existingTable);
							}

							IActiveFlagService<?> flagService = get(IActiveFlagService.class);
							return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
								.chain(archivedFlag -> {
									existingTable.setActiveFlagID(archivedFlag);
									existingTable.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									return session.merge(existingTable);
								})
								.chain(updatedTable -> {
									IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> newTableForClassification = get(getGeographyRelationshipClass());
									newTableForClassification.setId(null);
									newTableForClassification.setClassificationID(updatedTable.getClassificationID());
									newTableForClassification.setSystemID(system);
									newTableForClassification.setOriginalSourceSystemID(updatedTable.getId());
									newTableForClassification.setOriginalSourceSystemUniqueID(updatedTable.getId());
									newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
									newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));

									return flagService.getActiveFlag(session, system.getEnterpriseID(), identityToken)
										.map(activeFlag -> {
											newTableForClassification.setActiveFlagID(activeFlag);
											newTableForClassification.setValue(storeValue == null ? "" : storeValue);
											newTableForClassification.setEnterpriseID(system.getEnterpriseID());
											configureGeographyAddable(newTableForClassification, (J) updatedTable.getPrimary(), updatedTable.getSecondary(),
													classification, storeValue, system);
											return newTableForClassification;
										})
										.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
										.chain(table -> {
											// Start the createDefaultSecurity operation but don't wait for it to complete
											table.createDefaultSecurity(session, system, identityToken);
											// Return the original table immediately without waiting for createDefaultSecurity to complete
											return Uni.createFrom().item(updatedTable);
										});
								});
						});
				});
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> addOrReuseGeography(Mutiny.Session session, String classificationValue,
																				IGeography<?,?> geographyType,
																				String searchValue,
																				ISystems<?,?> system,
																				UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> tableForClassification = get(getGeographyRelationshipClass());

			return tableForClassification.builder(session)
				.findLink((J) this, null, null)
				.withValue(searchValue)
				.inActiveRange()
				.inDateRange()
				.withClassification(classificationValue, system)
				//.canCreate(system.getEnterpriseID(), identityToken)
				.get()
				.chain(existingTable -> {
					if (existingTable == null) {
						return addGeography(session, geographyType, classificationValue, searchValue, system, identityToken);
					} else {
						return Uni.createFrom().item(existingTable);
					}
				});
	}
}
