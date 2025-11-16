package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItemType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"UnusedReturnValue", "unused", "DuplicatedCode"})
public interface IManageResourceItemTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
	private String getResourceItemsRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XResourceItemType";
		return className;
	}

	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?>> getResourceItemTypeRelationshipClass()
	{
		String joinTableName = getResourceItemsRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find resourceItemType linked class - " + joinTableName, e);
		}
	}

	/**
	 * Finds a resource item type with the given classification name, resource type, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> findResourceItemType(Mutiny.Session session, String classificationName, String resourceType, String value, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

		return resourceItemService.findResourceItemType(session, resourceType, system, identityToken)
			.chain(resourceItemType -> tableForClassification.builder(session)
				.findLink((J) this, resourceItemType, value)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.get()
				.map(result -> {
					if (result == null) {
						throw new NoSuchElementException("Resource item type not found");
					}
					return (IRelationshipValue<J, IResourceItemType<?, ?>, ?>) result;
				})
			);
	}

	/**
	 * Checks if the entity has resource item types with the given classification name, resource item type name, and system.
	 */
	default Uni<Boolean> hasResourceItemTypes(Mutiny.Session session, String classificationName, String resourceItemTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		return numberOfResourceItemTypes(session, classificationName, resourceItemTypeName, system, identityToken)
			.map(count -> count > 0);
	}

	/**
	 * Gets the number of resource item types with the given classification name, resource item type name, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfResourceItemTypes(Mutiny.Session session, String classificationName, String resourceItemTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());

		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
		return resourceItemService.findResourceItemType(session, resourceItemTypeName, system, identityToken)
			.chain(resourceItemType -> tableForClassification.builder(session)
				.findLink((J) this, resourceItemType, null)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.getCount()
			);
	}

	/**
	 * Adds a resource item type with the given resource type, value, classification name, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> addResourceItemTypes(Mutiny.Session session, String resourceType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			return Uni.combine().all().unis(
					resourceItemService.findResourceItemType(session, resourceType, system, identityToken),
					classificationService.find(session, finalClassificationName, system, identityToken)
			).asTuple()
			.map(tuple -> {
				IResourceItemType<?, ?> resourceItemType = tuple.getItem1();
				IClassification<?, ?> classification = tuple.getItem2();

				tableForClassification.setEnterpriseID(system.getEnterpriseID());
				tableForClassification.setValue(value);
				tableForClassification.setSystemID(system);
				tableForClassification.setClassificationID(classification);
				tableForClassification.setOriginalSourceSystemID(system.getId());
				tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
				tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
				tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
				tableForClassification.setActiveFlagID(system.getActiveFlagID());
				configureResourceItemTypeLinkValue(tableForClassification, (J) this, resourceItemType, classification, value, system.getEnterpriseID());

				return tableForClassification;
			})
			.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
			.chain(table -> {
				// Start the createDefaultSecurity operation but don't wait for it to complete
				table.createDefaultSecurity(session, system, identityToken);
				// Return the table immediately without waiting for createDefaultSecurity to complete
				return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) table);
			});
	}

	/**
	 * Configures a resource item type link value.
	 * 
	 * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
	 * It doesn't need to return a Uni as it's a synchronous operation.
	 */
	@SuppressWarnings("rawtypes")
	void configureResourceItemTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IResourceItemType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

	/**
	 * Adds or reuses a resource item type with the given resource type name, classification name, search value, value, and system.
	 */
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> addOrReuseResourceItemTypes(Mutiny.Session session, String resourceTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName) 
					? DefaultClassifications.NoClassification.toString() 
					: classificationName;

			// First get the resource item type
			return resourceItemService.findResourceItemType(session, resourceTypeName, system, identityToken)
				.onItem().transformToUni(resourceItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, resourceItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.onFailure(NoResultException.class)
						.recoverWithUni(() -> {
							return (Uni) addResourceItemTypes(session, resourceTypeName, value, finalClassificationName, system, identityToken);
						})
						.chain(result -> {
							// Cast the result to the correct type and return it
							return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) result);
						});
				});
	}

	/**
	 * Adds or updates a resource item type with the given resource type name, classification name, search value, value, and system.
	 */
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> addOrUpdateResourceItemTypes(Mutiny.Session session, String resourceTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return Uni.combine().all().unis(
					resourceItemService.findResourceItemType(session, resourceTypeName, system, identityToken),
					classificationService.find(session, finalClassificationNameCopy, system, identityToken)
			).asTuple()
			.chain(tuple -> {
				IResourceItemType<?, ?> resourceItemType = tuple.getItem1();
				IClassification<?, ?> classification = tuple.getItem2();

				// Create a query to find the existing relationship
				return tableForClassification.builder(session)
					.findLink((J) this, resourceItemType, searchValue)
					.inActiveRange()
					.withClassification(finalClassificationNameCopy, system)
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
						configureResourceItemTypeLinkValue(tableForClassification, (J) this, resourceItemType, classification, value, system.getEnterpriseID());

						return (Uni) Uni.createFrom().item(tableForClassification)
							.chain(table -> {
								return session.persist(table).replaceWith(Uni.createFrom().item(table));
							})
							.chain(table -> {
								table.createDefaultSecurity(session, system, identityToken);
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) table);
							});
					})
					.chain(result -> {

						// Cast the result to the correct type
						IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> existingTable = 
							(IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?>) result;

						// If the value is the same, return the existing relation
						if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
							return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) existingTable);
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
								IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getResourceItemTypeRelationshipClass());
								newTableForClassification.setId(null);
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
										configureResourceItemTypeLinkValue(newTableForClassification, (J) this, resourceItemType, classification, value, system.getEnterpriseID());
										return newTableForClassification;
									});
							})
							.chain(newTable -> {
								return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
							})
							.chain(newTable -> {
								newTable.createDefaultSecurity(session, originalSystem, identityToken);
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) newTable);
							});
					});
			});
	}

	/**
	 * Updates a resource item type with the given resource type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> updateResourceItemTypes(Mutiny.Session session, String resourceTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return Uni.combine().all().unis(
					resourceItemService.findResourceItemType(session, resourceTypeName, system, identityToken),
					classificationService.find(session, finalClassificationNameCopy, system, identityToken)
			).asTuple()
			.chain(tuple -> {
				IResourceItemType<?, ?> resourceItemType = tuple.getItem1();
				IClassification<?, ?> classification = tuple.getItem2();

				// Create a query to find the existing relationship
				return tableForClassification.builder(session)
					.findLink((J) this, resourceItemType, searchValue)
					.inActiveRange()
					.withClassification(finalClassificationNameCopy, system)
					.inDateRange()
					.canRead(system, identityToken)
					.get()
					.chain(result -> {
						// If result is null, do nothing
						if (result == null) {
							return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) tableForClassification);
						}

						// Cast the result to the correct type
						IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> existingTable = 
								(IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?>) result;

						// If the value is the same, return the existing relation
						if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
							return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) existingTable);
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
									IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getResourceItemTypeRelationshipClass());
									newTableForClassification.setId(null);
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
												configureResourceItemTypeLinkValue(newTableForClassification, (J) this, resourceItemType, classification, value, system.getEnterpriseID());
												return newTableForClassification;
											});
								})
								.chain(newTable -> {
									return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
								})
								.chain(newTable -> {
									newTable.createDefaultSecurity(session, originalSystem, identityToken);
									return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) newTable);
								});
					});
			});
	}

	/**
	 * Expires a resource item type with the given resource type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> expireResourceItemTypes(Mutiny.Session session, String resourceTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return resourceItemService.findResourceItemType(session, resourceTypeName, system, identityToken)
				.chain(resourceItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, resourceItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationNameCopy, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> existingTable = 
									(IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) existingTable);
							}

							// Otherwise, expire the relation
							existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
							return session.merge(existingTable);
						});
				});
	}

	/**
	 * Archives a resource item type with the given resource type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> archiveResourceItemTypes(Mutiny.Session session, String resourceTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return resourceItemService.findResourceItemType(session, resourceTypeName, system, identityToken)
				.chain(resourceItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, resourceItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationNameCopy, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> existingTable = 
									(IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) existingTable);
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
	 * Removes a resource item type with the given resource type name, classification name, search value, value, and system.
	 */
	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IResourceItemType<?, ?>, ?>> removeResourceItemTypes(Mutiny.Session session, String resourceTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemTypeRelationshipClass());
			IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return resourceItemService.findResourceItemType(session, resourceTypeName, system, identityToken)
				.chain(resourceItemType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, resourceItemType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationNameCopy, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?> existingTable = 
									(IWarehouseRelationshipTable<?, ?, J, IResourceItemType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IResourceItemType<?, ?>, ?>) existingTable);
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
