package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.enumerations.OrderByType;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products.IProduct;
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

public interface IManageProducts<J extends IWarehouseBaseTable<J, ?,? extends Serializable>>
{
	private String getProductsRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XProduct";
		return className;
	}
	
	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?>> getProductRelationshipClass()
	{
		String joinTableName = getProductsRelationshipTable();
		try
		{
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find resourceItem linked class - " + joinTableName, e);
		}
	}
	
	
	
	/**
	 * Configures a product.
	 * 
	 * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
	 * It doesn't need to return a Uni as it's a synchronous operation.
	 */
	@SuppressWarnings("rawtypes")
	void configureProductAddable(Mutiny.StatelessSession session, IWarehouseRelationshipTable linkTable, J primary, IProduct<?,?> secondary, IClassification<?,?> classificationValue, String value, ISystems<?,?> system);
	
	
	
	
	
	
	
	// =============================================================================================
	// Stateless (Mutiny.StatelessSession) twins of the above. Reads are verbatim (the query builder is
	// session-polymorphic); writes swap session.fetch→system.getEnterprise(), session.persist→session.insert,
	// configure*Addable(session,…)→configure*Addable((Mutiny.StatelessSession) null,…) (the hook ignores the session),
	// and the managed per-row createDefaultSecurity→the stateless resolveDefaultGroupFolderTokens +
	// createDefaultSecurity path (tolerant). Same signatures/return types, first parameter only changed.
	// =============================================================================================

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProduct<?,?>, ?>> findByProduct(Mutiny.StatelessSession session, IProduct<?,?> allWithProduct, String classification, String value, boolean first, boolean latest, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> relationshipTable = get(getProductRelationshipClass());
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IEnterprise<?,?> enterprise = system.getEnterprise();
		return classificationService.find(session, classification, system, identityToken)
			.chain(iClassification -> {
				IQueryBuilderRelationships<?, ?, J, IProduct<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
						= relationshipTable.builder(session)
						                   .findLink(null, allWithProduct, null)
						                   .inActiveRange()
						                   .withClassification(classification, system)
						                   .withValue(value)
						                   .inDateRange()
						                   .withEnterprise(enterprise)
						                   .canRead(system, identityToken);
				if (first) { queryBuilderRelationshipClassification.setMaxResults(1); }
				if (latest) { queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"), OrderByType.DESC); }
				return queryBuilderRelationshipClassification.get()
					.onItem().ifNull().failWith(() -> new NoSuchElementException("Product not found"))
					.map(item -> (IRelationshipValue<J, IProduct<?,?>, ?>) item);
			});
	}

	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IProduct<?,?>, ?>>> findAllByProduct(Mutiny.StatelessSession session, IProduct<?,?> byType, String classification, String value, boolean first, boolean latest, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> relationshipTable = get(getProductRelationshipClass());
		IEnterprise<?,?> enterprise = system.getEnterprise();
		IQueryBuilderRelationships<?, ?, J, IProduct<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
				= relationshipTable.builder(session)
				                   .findLink(null, byType, null)
				                   .inActiveRange()
				                   .withClassification(classification, system)
				                   .withValue(value)
				                   .inDateRange()
				                   .withEnterprise(enterprise)
				                   .canRead(system, identityToken);
		if (first) { queryBuilderRelationshipClassification.setMaxResults(1); }
		if (latest) { queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"), OrderByType.DESC); }
		return queryBuilderRelationshipClassification.getAll()
			.map(list -> (List<IRelationshipValue<J, IProduct<?,?>, ?>>) list);
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> findProduct(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?,?> system, boolean first, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> relationshipTable = get(getProductRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IProduct<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
				= relationshipTable.builder(session)
				                   .findLink((J) this, null, null)
				                   .inActiveRange()
				                   .withClassification(classification, system)
				                   .withValue(searchValue)
				                   .inDateRange()
				                   .withEnterprise(system.getEnterprise())
				                   .canRead(system, identityToken);
		if (first) { queryBuilderRelationshipClassification.setMaxResults(1); }
		if (latest) { queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate")); }
		return queryBuilderRelationshipClassification.get()
			.onItem().ifNull().failWith(() -> new NoSuchElementException("Product not found"))
			.map(item -> (IRelationshipValue<J, IProduct<?, ?>, ?>) item);
	}

	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IProduct<?, ?>, ?>>> findProductsAll(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?,?> system, boolean latest, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> relationshipTable = get(getProductRelationshipClass());
		IQueryBuilderRelationships<?, ?, J, IProduct<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
				= relationshipTable.builder(session)
				                   .findLink((J) this, null, null)
				                   .inActiveRange()
				                   .withClassification(classification, system)
				                   .withValue(searchValue)
				                   .inDateRange()
				                   .withEnterprise(system.getEnterprise())
				                   .canRead(system, identityToken);
		if (latest) { queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate")); }
		return queryBuilderRelationshipClassification.getAll()
			.map(list -> (List<IRelationshipValue<J, IProduct<?, ?>, ?>>) list);
	}

	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfProducts(Mutiny.StatelessSession session, String classificationValue, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> relationshipTable = get(getProductRelationshipClass());
		final String finalClassificationValue = (classificationValue == null) ? DefaultClassifications.NoClassification.classificationValue() : classificationValue;
		return relationshipTable.builder(session)
		                        .findLink((J) this, null, value)
		                        .withClassification(finalClassificationValue, system)
		                        .inActiveRange()
		                        .inDateRange()
		                        .canRead(system, identityToken)
		                        .getCount();
	}

	default Uni<Boolean> hasProducts(Mutiny.StatelessSession session, String productTypeName, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		return numberOfProducts(session, productTypeName, searchValue, system, identityToken)
			.map(count -> count > 0);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> addProduct(Mutiny.StatelessSession session, IProduct<?,?> product, String classificationName, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductRelationshipClass());
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IEnterprise<?,?> enterprise = system.getEnterprise();
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
					configureProductAddable((Mutiny.StatelessSession) null, tableForClassification, (J) this, product, classification, value, system);
					com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
							(com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
					if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
					return session.insert(tableForClassification)
						.chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
							.chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
							.onFailure().recoverWithItem(0L))
						.replaceWith((IRelationshipValue<J, IProduct<?, ?>, ?>) tableForClassification);
				}));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> addOrReuseProduct(Mutiny.StatelessSession session, String classificationValue, IProduct<?,?> product, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductRelationshipClass());
		return tableForClassification.builder(session)
			.findLink((J) this, null, null)
			.withValue(searchValue)
			.inActiveRange()
			.inDateRange()
			.withClassification(classificationValue, system)
			.get()
			.onFailure(NoResultException.class)
			.recoverWithUni(() -> (Uni) addProduct(session, product, classificationValue, searchValue, system, identityToken))
			.chain(result -> Uni.createFrom().item((IRelationshipValue<J, IProduct<?, ?>, ?>) result));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> addOrUpdateProduct(Mutiny.StatelessSession session, String classificationValue, IProduct<?,?> product, String searchValue, String storeValue, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductRelationshipClass());
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IEnterprise<?,?> enterprise = system.getEnterprise();
		return classificationService.find(session, classificationValue, system, identityToken)
			.chain(classification -> tableForClassification.builder(session)
				.findLink((J) this, null, null)
				.withValue(searchValue)
				.inActiveRange()
				.inDateRange()
				.withClassification(classificationValue, system)
				.get()
				.onFailure(NoResultException.class)
				.recoverWithUni(() -> (Uni) addProduct(session, product, classificationValue, storeValue, system, identityToken))
				.chain(result -> {
					IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> existingTable =
						(IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?>) result;
					if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
						return Uni.createFrom().item((IRelationshipValue<J, IProduct<?, ?>, ?>) existingTable);
					}
					IActiveFlagService<?> flagService = get(IActiveFlagService.class);
					ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
					return flagService.getArchivedFlag(session, enterprise, identityToken)
						.chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(com.entityassist.RootEntity.getNow())))
						.chain(() -> {
							IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> newTableForClassification = get(getProductRelationshipClass());
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
									configureProductAddable((Mutiny.StatelessSession) null, newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(), classification, storeValue, system);
									com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
											(com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
									return session.insert(newTableForClassification)
										.chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
											.chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
											.onFailure().recoverWithItem(0L))
										.replaceWith((IRelationshipValue<J, IProduct<?, ?>, ?>) newTableForClassification);
								});
						});
				}));
	}
}
