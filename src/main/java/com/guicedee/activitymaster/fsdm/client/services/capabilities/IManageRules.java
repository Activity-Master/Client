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

	// =============================================================================================
	// Stateless (Mutiny.StatelessSession) twins. Reads are verbatim; writes swap
	// session.fetch→system.getEnterprise(), session.persist→session.insert, and the managed per-row
	// createDefaultSecurity→the stateless resolveDefaultGroupFolderTokens + createDefaultSecurity path.
	// =============================================================================================

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> findRules(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?,?> system, boolean first, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> relationshipTable = get(getRulesRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IRules<?, ?>, java.util.UUID> q
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
		return q.get()
			.onItem().ifNull().failWith(() -> new NoSuchElementException("Rule not found"))
			.map(item -> (IRelationshipValue<J, IRules<?, ?>, ?>) item);
	}

	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IRules<?, ?>, ?>>> findRulesAll(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?,?> system, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> relationshipTable = get(getRulesRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IRules<?, ?>, java.util.UUID> q
				= relationshipTable.builder(session)
				                   .findLink((J) this, null, null)
				                   .inActiveRange()
				                   .withClassification(classification, system)
				                   .withValue(searchValue)
				                   .inDateRange()
				                   .withEnterprise(system.getEnterprise())
				                   .canRead(system, identityToken);
		if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
		return q.getAll().map(list -> (List<IRelationshipValue<J, IRules<?, ?>, ?>>) list);
	}

	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfRuless(Mutiny.StatelessSession session, String classificationValue, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> relationshipTable = get(getRulesRelationshipClass());
		final String finalClassificationValue = classificationValue == null ? DefaultClassifications.NoClassification.classificationValue() : classificationValue;
		return relationshipTable.builder(session)
				.findLink((J) this, null, value)
				.withClassification(finalClassificationValue, system)
				.inActiveRange()
				.inDateRange()
				.canRead(system, identityToken)
				.getCount();
	}

	default Uni<Boolean> hasRuless(Mutiny.StatelessSession session, String resourceItemTypeName, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		return numberOfRuless(session, resourceItemTypeName, searchValue, system, identityToken).map(count -> count > 0);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> addRules(Mutiny.StatelessSession session, IRules<?,?> resourceItem, String classificationName, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> tableForClassification = get(getRulesRelationshipClass());
		IClassificationService<?> classificationService = get(IClassificationService.class);
		final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
		final var enterprise = system.getEnterprise();
		IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
		ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
		return classificationService.find(session, finalClassificationName, system, identityToken)
			.chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
				.chain(activeFlag -> {
					tableForClassification.setEnterpriseID(enterprise);
					tableForClassification.setValue(Strings.nullToEmpty(value));
					tableForClassification.setSystemID(system);
					tableForClassification.setOriginalSourceSystemID(system.getId());
					tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
					tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
					tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
					tableForClassification.setActiveFlagID(activeFlag);
					tableForClassification.setClassificationID(classification);
					configureRulesAddable(tableForClassification, (J) this, resourceItem, classification, value, system);
					com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
							(com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
					if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
					return session.insert(tableForClassification)
						.chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
							.chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
							.onFailure().recoverWithItem(0L))
						.replaceWith((IRelationshipValue<J, IRules<?, ?>, ?>) tableForClassification);
				}));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> addOrReuseRules(Mutiny.StatelessSession session, String classificationValue, IRules<?,?> resourceItemType, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> tableForClassification = get(getRulesRelationshipClass());
		final String finalClassificationName = Strings.isNullOrEmpty(classificationValue) ? DefaultClassifications.NoClassification.toString() : classificationValue;
		return tableForClassification.builder(session)
			.findLink((J) this, null, searchValue)
			.inActiveRange()
			.withClassification(finalClassificationName, system)
			.inDateRange()
			.get()
			.onFailure(NoResultException.class)
			.recoverWithUni(() -> (Uni) addRules(session, resourceItemType, finalClassificationName, searchValue, system, identityToken))
			.chain(result -> Uni.createFrom().item((IRelationshipValue<J, IRules<?, ?>, ?>) result));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IRules<?, ?>, ?>> addOrUpdateRules(Mutiny.StatelessSession session, String classificationValue, IRules<?,?> resourceItemType, String searchValue, String storeValue, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> tableForClassification = get(getRulesRelationshipClass());
		IClassificationService<?> classificationService = get(IClassificationService.class);
		final String finalClassificationName = Strings.isNullOrEmpty(classificationValue) ? DefaultClassifications.NoClassification.toString() : classificationValue;
		final var enterprise = system.getEnterprise();
		return classificationService.find(session, finalClassificationName, system, identityToken)
			.chain(classification -> tableForClassification.builder(session)
				.findLink((J) this, null, searchValue)
				.inActiveRange()
				.withClassification(finalClassificationName, system)
				.inDateRange()
				.get()
				.onFailure(NoResultException.class)
				.recoverWithUni(() -> (Uni) addRules(session, resourceItemType, finalClassificationName, storeValue, system, identityToken))
				.chain(result -> {
					IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> existingTable =
						(IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?>) result;
					if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
						return Uni.createFrom().item((IRelationshipValue<J, IRules<?, ?>, ?>) existingTable);
					}
					IActiveFlagService<?> flagService = get(IActiveFlagService.class);
					ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
					return flagService.getArchivedFlag(session, enterprise, identityToken)
						.chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
						.chain(() -> {
							IWarehouseRelationshipTable<?, ?, J, IRules<?, ?>, java.util.UUID, ?> newTableForClassification = get(getRulesRelationshipClass());
							newTableForClassification.setId(null);
							newTableForClassification.setClassificationID(existingTable.getClassificationID());
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
									newTableForClassification.setValue(storeValue == null ? "" : storeValue);
									newTableForClassification.setEnterpriseID(enterprise);
									configureRulesAddable(newTableForClassification, existingTable.getPrimary(), existingTable.getSecondary(), classification, storeValue, system);
									com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
											(com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
									return session.insert(newTableForClassification)
										.chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
											.chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
											.onFailure().recoverWithItem(0L))
										.replaceWith((IRelationshipValue<J, IRules<?, ?>, ?>) newTableForClassification);
								});
						});
				}));
	}

}

