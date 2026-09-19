
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

	// =============================================================================================
	// Stateless (Mutiny.StatelessSession) twins. Reads verbatim; writes use session.insert +
	// system.getEnterprise() + the stateless resolveDefaultGroupFolderTokens/createDefaultSecurity path.
	// =============================================================================================

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> findGeography(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?,?> system, boolean first, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID,?> relationshipTable = get(getGeographyRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IGeography<?, ?>, java.util.UUID> q
				= relationshipTable.builder(session)
				                   .findLink((J) this, null, null)
				                   .inActiveRange()
				                   .withClassification(classification, system)
				                   .withValue(searchValue)
				                   .inDateRange()
				                   .withEnterprise(system.getEnterprise())
				                   .canRead(system, identityToken);
		if (first) { q.setMaxResults(1); }
		if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
		return (Uni<IRelationshipValue<J, IGeography<?, ?>, ?>>) q.get()
				.onItem().ifNull().failWith(() -> new NoSuchElementException("Geography not found"));
	}

	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IGeography<?, ?>, ?>>> findGeographysAll(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?,?> system, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> relationshipTable = get(getGeographyRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IGeography<?, ?>, java.util.UUID> q
				= relationshipTable.builder(session)
				                   .findLink((J) this, null, null)
				                   .inActiveRange()
				                   .withClassification(classification, system)
				                   .withValue(searchValue)
				                   .inDateRange()
				                   .withEnterprise(system.getEnterprise())
				                   .canRead(system, identityToken);
		if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
		return q.getAll().map(list -> (List<IRelationshipValue<J, IGeography<?, ?>, ?>>) list);
	}

	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfGeography(Mutiny.StatelessSession session, String classificationValue, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> relationshipTable = get(getGeographyRelationshipClass());
		final String finalClassificationValue = classificationValue == null ? DefaultClassifications.NoClassification.classificationValue() : classificationValue;
		return relationshipTable.builder(session)
		                        .findLink((J) this, null, value)
		                        .withClassification(finalClassificationValue, system)
		                        .inActiveRange()
		                        .inDateRange()
		                        .canRead(system, identityToken)
		                        .getCount();
	}

	default Uni<Boolean> hasGeography(Mutiny.StatelessSession session, String geographyTypeName, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		return numberOfGeography(session, geographyTypeName, searchValue, system, identityToken).map(count -> count > 0);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> addGeography(Mutiny.StatelessSession session, IGeography<?,?> geography, String classificationName, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> tableForClassification = get(getGeographyRelationshipClass());
		IClassificationService<?> classificationService = get(IClassificationService.class);
		final var enterprise = system.getEnterprise();
		IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
		ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
		return classificationService.find(session, classificationName, system, identityToken)
			.chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
				.chain(activeFlag -> {
					tableForClassification.setEnterpriseID(enterprise);
					tableForClassification.setValue(Strings.nullToEmpty(value));
					tableForClassification.setSystemID(system);
					tableForClassification.setOriginalSourceSystemID(system.getId());
					tableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
					tableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
					tableForClassification.setActiveFlagID(activeFlag);
					tableForClassification.setClassificationID(classification);
					configureGeographyAddable(tableForClassification, (J) this, geography, classification, value, system);
					com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
							(com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
					if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
					return session.insert(tableForClassification)
						.chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
							.chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
							.onFailure().recoverWithItem(0L))
						.replaceWith((IRelationshipValue<J, IGeography<?, ?>, ?>) tableForClassification);
				}));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> addOrReuseGeography(Mutiny.StatelessSession session, String classificationValue, IGeography<?,?> geographyType, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> tableForClassification = get(getGeographyRelationshipClass());
		return tableForClassification.builder(session)
			.findLink((J) this, null, null)
			.withValue(searchValue)
			.inActiveRange()
			.inDateRange()
			.withClassification(classificationValue, system)
			.get()
			.chain(existingTable -> existingTable == null
					? addGeography(session, geographyType, classificationValue, searchValue, system, identityToken)
					: Uni.createFrom().item(existingTable));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IGeography<?, ?>, ?>> addOrUpdateGeography(Mutiny.StatelessSession session, String classificationValue, IGeography<?,?> geographyType, String searchValue, String storeValue, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> tableForClassification = get(getGeographyRelationshipClass());
		IClassificationService<?> classificationService = get(IClassificationService.class);
		final var enterprise = system.getEnterprise();
		return classificationService.find(session, classificationValue, system, identityToken)
			.chain(classification -> tableForClassification.builder(session)
				.findLink((J) this, null, null)
				.withValue(searchValue)
				.inActiveRange()
				.inDateRange()
				.withClassification(classificationValue, system)
				.get()
				.chain(existingTable -> {
					if (existingTable == null) {
						return addGeography(session, geographyType, classificationValue, storeValue, system, identityToken);
					}
					if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
						return Uni.createFrom().item(existingTable);
					}
					IActiveFlagService<?> flagService = get(IActiveFlagService.class);
					ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
					return flagService.getArchivedFlag(session, enterprise, identityToken)
						.chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(com.entityassist.RootEntity.getNow())))
						.chain(retiredCount -> {
							IWarehouseRelationshipTable<?, ?, J, IGeography<?, ?>, java.util.UUID, ?> newTableForClassification = get(getGeographyRelationshipClass());
							newTableForClassification.setId(null);
							newTableForClassification.setClassificationID(existingTable.getClassificationID());
							newTableForClassification.setSystemID(system);
							newTableForClassification.setOriginalSourceSystemID(existingTable.getId());
							newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
							newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
							newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
							newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
							newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
							return flagService.getActiveFlag(session, enterprise, identityToken)
								.chain(activeFlag -> {
									newTableForClassification.setActiveFlagID(activeFlag);
									newTableForClassification.setValue(storeValue == null ? "" : storeValue);
									newTableForClassification.setEnterpriseID(enterprise);
									configureGeographyAddable(newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(), classification, storeValue, system);
									com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
											(com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
									return session.insert(newTableForClassification)
										.chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
											.chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
											.onFailure().recoverWithItem(0L))
										.replaceWith((IRelationshipValue<J, IGeography<?, ?>, ?>) newTableForClassification);
								});
						});
				}));
	}
}
