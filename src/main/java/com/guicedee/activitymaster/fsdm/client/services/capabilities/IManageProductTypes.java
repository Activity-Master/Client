package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products.IProductType;
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

@SuppressWarnings({"DuplicatedCode", "unused", "rawtypes"})
public interface IManageProductTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
	private String getProductTypeRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XProductType";
		return className;
	}

	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>> getProductTypeRelationshipClass()
	{
		String joinTableName = getProductTypeRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find productItemType linked class - " + joinTableName, e);
		}
	}

	/**
	 * Finds a product type with the given classification name, product type, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> findProductType(Mutiny.Session session, String classificationName, String productType, String value, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
		IProductService<?> productService = get(IProductService.class);

		return productService.findProductTypeForProduct(session, productType, system, identityToken)
			.chain(productItemType -> tableForClassification.builder(session)
				.findLink((J) this, productItemType, value)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.get()
				.onItem().ifNull().failWith(() -> new NoSuchElementException("Product type not found"))
				.map(item -> (IRelationshipValue<J, IProductType<?, ?>, ?>) item));
	}

	/**
	 * Finds all product types with the given classification name, product type, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IProductType<?, ?>, ?>>> findProductTypesAll(Mutiny.Session session, String classificationName, String productType, String value, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
		IProductService<?> productService = get(IProductService.class);

		return productService.findProductTypeForProduct(session, productType, system, identityToken)
			.chain(productItemType -> tableForClassification.builder(session)
				.findLink((J) this, productItemType, value)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.getAll()
				.map(list -> (List<IRelationshipValue<J, IProductType<?, ?>, ?>>) list));
	}

	/**
	 * Checks if the entity has product types with the given classification name, product type name, and system.
	 */
	default Uni<Boolean> hasProductTypes(Mutiny.Session session, String classificationName, String productTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		return numberOfProductTypes(session, classificationName, productTypeName, system, identityToken)
			.map(count -> count > 0);
	}

	/**
	 * Gets the number of product types with the given classification name, product item type name, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfProductTypes(Mutiny.Session session, String classificationName, String productItemTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
		IProductService<?> productService = get(IProductService.class);

		return productService.findProductTypeForProduct(session, productItemTypeName, system, identityToken)
			.chain(productType -> tableForClassification.builder(session)
				.findLink((J) this, productType, null)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.getCount());
	}

	/**
	 * Adds a product type with the given product type, value, classification name, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addProductTypes(Mutiny.Session session, String productType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
			IProductService<?> productService = get(IProductService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);
	
			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}
	
			final String finalClassificationNameCopy = finalClassificationName;
	
			// Sequentially fetch product type then classification to avoid parallel session operations
			return productService.findProductTypeForProduct(session, productType, system, identityToken)
				.chain(productItemType -> classificationService.find(session, finalClassificationNameCopy, system, identityToken)
					.map(classification -> new Object[]{productItemType, classification}))
				.chain(arr -> {
					IProductType<?, ?> productItemType = (IProductType<?, ?>) arr[0];
					IClassification<?, ?> classification = (IClassification<?, ?>) arr[1];
					
					// Set up the table
					tableForClassification.setEnterpriseID(system.getEnterpriseID());
					tableForClassification.setValue(value);
					tableForClassification.setSystemID(system);
					tableForClassification.setClassificationID(classification);
					tableForClassification.setOriginalSourceSystemID(system.getId());
					tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
					tableForClassification.setActiveFlagID(system.getActiveFlagID());
					configureProductTypeLinkValue(tableForClassification, (J) this, productItemType, classification, value, system.getEnterpriseID());
					
					return Uni.createFrom().item(tableForClassification)
						.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
						.chain(table -> {
							// Chain the security setup operation
							return table.createDefaultSecurity(session, system, identityToken)
								.onFailure().recoverWithNull()  // Continue even if security setup fails
								.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) table));
						});
				});
	}

	/**
	 * Configures a product type link value.
	 * 
	 * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
	 * It doesn't need to return a Uni as it's a synchronous operation.
	 */
	void configureProductTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IProductType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

	/**
	 * Adds or reuses a product type with the given product type name, classification name, search value, value, and system.
	 */
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addOrReuseProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
			IProductService<?> productService = get(IProductService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName) 
					? DefaultClassifications.NoClassification.toString() 
					: classificationName;

			// First get the product type
			return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
				.onItem().transformToUni(productItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, productItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.onFailure(NoResultException.class)
						.recoverWithUni(() -> {
							return (Uni) addProductTypes(session, productTypeName, value, finalClassificationName, system, identityToken);
						})
						.chain(result -> {
							// Cast the result to the correct type and return it
							return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) result);
						});
				});
	}

	/**
	 * Adds or updates a product type with the given product type name, classification name, search value, value, and system.
	 */
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addOrUpdateProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
			IProductService<?> productService = get(IProductService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName)
					? DefaultClassifications.NoClassification.toString()
					: classificationName;

			// Use Uni.combine to run both operations in parallel
			return Uni.combine().all().unis(
					productService.findProductTypeForProduct(session, productTypeName, system, identityToken),
					classificationService.find(session, finalClassificationName, system, identityToken)
				).asTuple()
				.chain(tuple -> {
					IProductType<?, ?> productItemType = tuple.getItem1();
					IClassification<?, ?> classification = tuple.getItem2();

					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, productItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
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
							tableForClassification.setClassificationID(classification);
							configureProductTypeLinkValue(tableForClassification, (J) this, productItemType, classification, value, system.getEnterpriseID());

							return (Uni) Uni.createFrom().item(tableForClassification)
								.chain(table -> {
									return session.merge(table);
								})
								.chain(table -> {
									// Chain the security setup operation
									return table.createDefaultSecurity(session, system, identityToken)
										.onFailure().recoverWithNull()  // Continue even if security setup fails
										.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) table));
								});
						})
						.chain(result -> {

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) existingTable);
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
									IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getProductTypeRelationshipClass());
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
											configureProductTypeLinkValue(newTableForClassification, (J) this, productItemType, classification, value, system.getEnterpriseID());
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
										.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) newTable));
								});
						});
				});
	}

	/**
	 * Updates a product type with the given product type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> updateProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
			IProductService<?> productService = get(IProductService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName)
					? DefaultClassifications.NoClassification.toString()
					: classificationName;

			// Use Uni.combine to run both operations in parallel
			return Uni.combine().all().unis(
					productService.findProductTypeForProduct(session, productTypeName, system, identityToken),
					classificationService.find(session, finalClassificationName, system, identityToken)
				).asTuple()
				.chain(tuple -> {
					IProductType<?, ?> productItemType = tuple.getItem1();
					IClassification<?, ?> classification = tuple.getItem2();

					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, productItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) existingTable);
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
									IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getProductTypeRelationshipClass());
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
											configureProductTypeLinkValue(newTableForClassification, (J) this, productItemType, classification, value, system.getEnterpriseID());
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
										.replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) newTable));
								});
						});
				});
	}

	/**
	 * Expires a product type with the given product type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> expireProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
			IProductService<?> productService = get(IProductService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName)
					? DefaultClassifications.NoClassification.toString()
					: classificationName;

			return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
				.chain(productItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, productItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) existingTable);
							}

							// Otherwise, expire the relation
							existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
							return session.merge(existingTable);
						});
				});
	}

	/**
	 * Archives a product type with the given product type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> archiveProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
			IProductService<?> productService = get(IProductService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName)
					? DefaultClassifications.NoClassification.toString()
					: classificationName;

			return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
				.chain(productItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, productItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) existingTable);
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
	 * Removes a product type with the given product type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> removeProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
			IProductService<?> productService = get(IProductService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName)
					? DefaultClassifications.NoClassification.toString()
					: classificationName;

			return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
				.chain(productItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, productItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) existingTable);
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