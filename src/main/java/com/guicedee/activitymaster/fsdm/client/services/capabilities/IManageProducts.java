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
	 * Finds a product with the given product, classification, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProduct<?,?>, ?>> findByProduct(Mutiny.Session session, IProduct<?,?> allWithProduct, String classification, String value, boolean first, boolean latest, ISystems<?,?> system, UUID... identityToken)
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
				if (first)
				{ 
					queryBuilderRelationshipClassification.setMaxResults(1); 
				}
				if (latest)
				{ 
					queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"), OrderByType.DESC); 
				}
				
				return queryBuilderRelationshipClassification.get()
					.onItem().ifNull().failWith(() -> new NoSuchElementException("Product not found"))
					.map(item -> (IRelationshipValue<J, IProduct<?,?>, ?>) item);
			});
	}
	
	/**
	 * Finds all products with the given product, classification, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IProduct<?,?>, ?>>> findAllByProduct(Mutiny.Session session, IProduct<?,?> byType, String classification, String value, boolean first, boolean latest, ISystems<?,?> system, UUID... identityToken)
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
		if (first)
		{ 
			queryBuilderRelationshipClassification.setMaxResults(1); 
		}
		if (latest)
		{ 
			queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"), OrderByType.DESC); 
		}
		
		return queryBuilderRelationshipClassification.getAll()
			.map(list -> (List<IRelationshipValue<J, IProduct<?,?>, ?>>) list);
	}
	
	/**
	 * Configures a product.
	 * 
	 * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
	 * It doesn't need to return a Uni as it's a synchronous operation.
	 */
	@SuppressWarnings("rawtypes")
	void configureProductAddable(Mutiny.Session session, IWarehouseRelationshipTable linkTable, J primary, IProduct<?,?> secondary, IClassification<?,?> classificationValue, String value, ISystems<?,?> system);
	
	/**
	 * Finds a product with the given classification, search value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> findProduct(Mutiny.Session session, String classification, String searchValue, ISystems<?,?> system, boolean first, boolean latest, UUID... identityToken)
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
		if (first)
		{
			queryBuilderRelationshipClassification.setMaxResults(1);
		}
		if (latest)
		{
			queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
		}
		
		return queryBuilderRelationshipClassification.get()
			.onItem().ifNull().failWith(() -> new NoSuchElementException("Product not found"))
			.map(item -> (IRelationshipValue<J, IProduct<?, ?>, ?>) item);
	}
	
	/**
	 * Finds all products with the given classification, search value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IProduct<?, ?>, ?>>> findProductsAll(Mutiny.Session session, String classification, String searchValue, ISystems<?,?> system, boolean latest, UUID... identityToken)
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
		if (latest)
		{
			queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
		}
		
		return queryBuilderRelationshipClassification.getAll()
			.map(list -> (List<IRelationshipValue<J, IProduct<?, ?>, ?>>) list);
	}
	
	/**
	 * Gets the number of products with the given classification value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfProducts(Mutiny.Session session, String classificationValue, String value, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> relationshipTable = get(getProductRelationshipClass());
		
		if (classificationValue == null)
		{
			classificationValue = DefaultClassifications.NoClassification.classificationValue();
		}
		
		final String finalClassificationValue = classificationValue;
		
		return relationshipTable.builder(session)
		                        .findLink((J) this, null, value)
		                        .withClassification(finalClassificationValue, system)
		                        .inActiveRange()
		                        .inDateRange()
		                        .canRead(system, identityToken)
		                        .getCount();
	}
	
	/**
	 * Checks if the entity has products with the given product type name, search value, and system.
	 */
	default Uni<Boolean> hasProducts(Mutiny.Session session, String productTypeName, String searchValue, ISystems<?,?> system, UUID... identityToken)
	{
		return numberOfProducts(session, productTypeName, searchValue, system, identityToken)
			.map(count -> count > 0);
	}
	
	/**
	 * Adds a product with the given product, classification name, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> addProduct(Mutiny.Session session, IProduct<?,?> product,
																	 String classificationName,
																	 String value,
																	 ISystems<?,?> system,
																	 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductRelationshipClass());
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
					
					configureProductAddable(session, tableForClassification, (J) this,
							product,
							classification, value, system);
					
					return tableForClassification;
				})
				.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
				.chain(table -> {
					// Chain the security setup operation
					return table.createDefaultSecurity(session, system, identityToken)
						.onFailure().recoverWithNull()  // Continue even if security setup fails
						.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProduct<?, ?>, ?>) table));
				});
	}
	
	/**
	 * Adds or updates a product with the given classification value, product type, search value, store value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> addOrUpdateProduct(Mutiny.Session session, String classificationValue,
																			 IProduct<?,?> product,
																			 String searchValue,
																			 String storeValue,
																			 ISystems<?,?> system,
																			 UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductRelationshipClass());
			IClassificationService<?> classificationService = get(IClassificationService.class);
			
			return classificationService.find(session, classificationValue, system, identityToken)
				.chain(classification -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, null, null)
						.withValue(searchValue)
						.inActiveRange()
						.inDateRange()
						.withClassification(classificationValue, system)
						//.canCreate(system.getEnterpriseID(), identityToken)
						.get()
						.onFailure(NoResultException.class)
						.recoverWithUni(() -> {
							return (Uni) addProduct(session, product, classificationValue, storeValue, system, identityToken);
						})
						.chain(result -> {
							
							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?>) result;
							
							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(storeValue).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IProduct<?, ?>, ?>) existingTable);
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
									
									return flagService.getActiveFlag(session, system.getEnterpriseID(), identityToken)
										.map(activeFlag -> {
											newTableForClassification.setActiveFlagID(activeFlag);
											newTableForClassification.setValue(storeValue == null ? "" : storeValue);
											newTableForClassification.setEnterpriseID(system.getEnterpriseID());
											configureProductAddable(session, newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(),
													classification, storeValue, system);
											return newTableForClassification;
										});
								})
								.chain(newTable -> {
									return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
								})
								.chain(newTable -> {
									// Chain the security setup operation
									return newTable.createDefaultSecurity(session, system, identityToken)
										.onFailure().recoverWithNull()  // Continue even if security setup fails
										.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProduct<?, ?>, ?>) newTable));
								});
						});
				});
	}
	
	/**
	 * Adds or reuses a product with the given classification value, product type, search value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProduct<?, ?>, ?>> addOrReuseProduct(Mutiny.Session session, String classificationValue,
																			IProduct<?,?> product,
																			String searchValue,
																			ISystems<?,?> system,
																			UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProduct<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductRelationshipClass());
			
			// Create a query to find the existing relationship
			return tableForClassification.builder(session)
				.findLink((J) this, null, null)
				.withValue(searchValue)
				.inActiveRange()
				.inDateRange()
				.withClassification(classificationValue, system)
				//.canCreate(system.getEnterpriseID(), identityToken)
				.get()
				.onFailure(NoResultException.class)
				.recoverWithUni(() -> {
					return (Uni) addProduct(session, product, classificationValue, searchValue, system, identityToken);
				})
				.chain(result -> {
					
					// Otherwise, return the existing relation
					return Uni.createFrom().item((IRelationshipValue<J, IProduct<?, ?>, ?>) result);
				});
	}
}