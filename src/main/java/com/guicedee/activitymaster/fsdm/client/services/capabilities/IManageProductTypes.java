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
public interface IManageProductTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>> {
    private String getProductTypeRelationshipTable() {
        String className = getClass().getCanonicalName() + "XProductType";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>> getProductTypeRelationshipClass() {
        String joinTableName = getProductTypeRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find productItemType linked class - " + joinTableName, e);
        }
    }

    /**
     * Finds a product type with the given classification name, product type, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> findProductType(Mutiny.Session session, String classificationName, String productType, String value, ISystems<?, ?> system, UUID... identityToken) {
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
    default Uni<List<IRelationshipValue<J, IProductType<?, ?>, ?>>> findProductTypesAll(Mutiny.Session session, String classificationName, String productType, String value, ISystems<?, ?> system, UUID... identityToken) {
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
    default Uni<Boolean> hasProductTypes(Mutiny.Session session, String classificationName, String productTypeName, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfProductTypes(session, classificationName, productTypeName, system, identityToken)
                .map(count -> count > 0);
    }

    /**
     * Gets the number of product types with the given classification name, product item type name, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfProductTypes(Mutiny.Session session, String classificationName, String productItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
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
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addProductTypes(Mutiny.Session session, String productType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
        IProductService<?> productService = get(IProductService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName)) {
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

                    return session.fetch(system)
                            .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                    .chain(enterprise -> {
                                        IActiveFlagService<?> activeFlagSvc = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
                                        return activeFlagSvc.getActiveFlag(session, enterprise)
                                                .chain(activeFlag -> {
                                                    // Set up the table
                                                    tableForClassification.setEnterpriseID(enterprise);
                                                    tableForClassification.setValue(value);
                                                    tableForClassification.setSystemID(fetchedSystem);
                                                    tableForClassification.setClassificationID(classification);
                                                    tableForClassification.setOriginalSourceSystemID(fetchedSystem.getId());
                                                    tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                                    tableForClassification.setActiveFlagID(activeFlag);
                                                    configureProductTypeLinkValue(tableForClassification, (J) this, productItemType, classification, value, enterprise);

                                                    return Uni.createFrom().item(tableForClassification)
                                                            .chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
                                                            .chain(table -> {
                                                                // Chain the security setup operation
                                                                return table.createDefaultSecurity(session, system, identityToken)
                                                                        .onFailure().recoverWithNull()  // Continue even if security setup fails
                                                                        .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) table));
                                                            });
                                                });
                                    }));
                });
    }

    /**
     * Configures a product type link value.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    void configureProductTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IProductType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

    /**
     * Adds or reuses a product type with the given product type name, classification name, search value, value, and system.
     */
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addOrReuseProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
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
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addOrUpdateProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
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
                                return session.fetch(system)
                                        .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                .chain(enterprise -> {
                                                    IActiveFlagService<?> activeFlagSvc = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
                                                    return activeFlagSvc.getActiveFlag(session, enterprise)
                                                            .chain(activeFlag -> {
                                                                tableForClassification.setEnterpriseID(enterprise);
                                                                tableForClassification.setValue(value);
                                                                tableForClassification.setSystemID(fetchedSystem);
                                                                tableForClassification.setOriginalSourceSystemID(fetchedSystem.getId());
                                                                tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                                                tableForClassification.setActiveFlagID(activeFlag);
                                                                tableForClassification.setClassificationID(classification);
                                                                configureProductTypeLinkValue(tableForClassification, (J) this, productItemType, classification, value, enterprise);

                                                                return (Uni) Uni.createFrom().item(tableForClassification)
                                                                        .chain(session::merge)
                                                                        .chain(table -> {
                                                                            // Chain the security setup operation
                                                                            return table.createDefaultSecurity(session, system, identityToken)
                                                                                    .onFailure().recoverWithNull()  // Continue even if security setup fails
                                                                                    .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) table));
                                                                        });
                                                            });
                                                }));
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

                                return session.fetch(system)
                                        .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                .chain(systemEnterprise -> session.fetch(originalSystem)
                                                        .chain(fetchedOriginalSystem -> session.fetch(fetchedOriginalSystem.getEnterpriseID())
                                                                .chain(originalEnterprise -> flagService.getArchivedFlag(session, systemEnterprise, identityToken)
                                                                        .chain(archivedFlag -> {
                                                                            // Retire the current active row via a bulk UPDATE (bypasses the persistence context) so it
                                                                            // is closed without detaching the managed entity, which would corrupt the following insert.
                                                                            return SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag,
                                                                                    convertToUTCDateTime(RootEntity.getNow()));
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

                                                                            return flagService.getActiveFlag(session, originalEnterprise, identityToken)
                                                                                    .map(activeFlag -> {
                                                                                        newTableForClassification.setActiveFlagID(activeFlag);
                                                                                        newTableForClassification.setValue(value);
                                                                                        newTableForClassification.setEnterpriseID(systemEnterprise);
                                                                                        configureProductTypeLinkValue(newTableForClassification, (J) this, productItemType, classification, value, systemEnterprise);
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
                                                                        })))));
                            });
                });
    }

    /**
     * Updates a product type with the given product type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> updateProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
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

                                return session.fetch(system)
                                        .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                .chain(systemEnterprise -> session.fetch(originalSystem)
                                                        .chain(fetchedOriginalSystem -> session.fetch(fetchedOriginalSystem.getEnterpriseID())
                                                                .chain(originalEnterprise -> flagService.getArchivedFlag(session, systemEnterprise, identityToken)
                                                                        .chain(archivedFlag -> {
                                                                            // Retire the current active row via a bulk UPDATE (bypasses the persistence context) so it
                                                                            // is closed without detaching the managed entity, which would corrupt the following insert.
                                                                            return SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag,
                                                                                    convertToUTCDateTime(RootEntity.getNow()));
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

                                                                            return flagService.getActiveFlag(session, originalEnterprise, identityToken)
                                                                                    .map(activeFlag -> {
                                                                                        newTableForClassification.setActiveFlagID(activeFlag);
                                                                                        newTableForClassification.setValue(value);
                                                                                        newTableForClassification.setEnterpriseID(systemEnterprise);
                                                                                        configureProductTypeLinkValue(newTableForClassification, (J) this, productItemType, classification, value, systemEnterprise);
                                                                                        return newTableForClassification;
                                                                                    });
                                                                        })
                                                                        .chain(newTable -> {
                                                                            // Chain the security setup operation
                                                                            return newTable.createDefaultSecurity(session, originalSystem, identityToken)
                                                                                    .onFailure().recoverWithNull()  // Continue even if security setup fails
                                                                                    .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) newTable));
                                                                        })))));
                            });
                });
    }

    /**
     * Expires a product type with the given product type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> expireProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
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

                                // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                session.detach(existingTable);
                                existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                return session.merge(existingTable);
                            });
                });
    }

    /**
     * Archives a product type with the given product type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> archiveProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
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

                                return session.fetch(system)
                                        .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                .chain(enterprise -> flagService.getArchivedFlag(session, enterprise, identityToken)
                                                        .chain(archivedFlag -> {
                                                            // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                                            // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                                            session.detach(existingTable);
                                                            existingTable.setActiveFlagID(archivedFlag);
                                                            existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                                            return session.merge(existingTable);
                                                        })));
                            });
                });
    }

    /**
     * Removes a product type with the given product type name, classification name, search value, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> removeProductTypes(Mutiny.Session session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
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

                                return session.fetch(system)
                                        .chain(fetchedSystem -> session.fetch(fetchedSystem.getEnterpriseID())
                                                .chain(enterprise -> flagService.getDeletedFlag(session, enterprise, identityToken)
                                                        .chain(deletedFlag -> {
                                                            // Detach so the merge is an explicit update of a detached instance; under Hibernate
                                                            // Reactive bytecode enhancement mutating a managed entity + merge is a no-op (not flushed).
                                                            session.detach(existingTable);
                                                            existingTable.setActiveFlagID(deletedFlag);
                                                            existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                                            return session.merge(existingTable);
                                                        })));
                            });
                });
    }

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins of the read + create family. The secondary IProductType
    // is resolved via the stateless IProductService.findProductTypeForProduct; writes use session.insert +
    // system.getEnterprise() + the stateless default-security path. configureProductTypeLinkValue is
    // session-free. update / expire / archive / remove (session.merge based) are not twinned here.
    // =============================================================================================

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> findProductType(Mutiny.StatelessSession session, String classificationName, String productType, String value, ISystems<?, ?> system, UUID... identityToken) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<List<IRelationshipValue<J, IProductType<?, ?>, ?>>> findProductTypesAll(Mutiny.StatelessSession session, String classificationName, String productType, String value, ISystems<?, ?> system, UUID... identityToken) {
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

    default Uni<Boolean> hasProductTypes(Mutiny.StatelessSession session, String classificationName, String productTypeName, ISystems<?, ?> system, UUID... identityToken) {
        return numberOfProductTypes(session, classificationName, productTypeName, system, identityToken).map(count -> count > 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Long> numberOfProductTypes(Mutiny.StatelessSession session, String classificationName, String productItemTypeName, ISystems<?, ?> system, UUID... identityToken) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addProductTypes(Mutiny.StatelessSession session, String productType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
        IProductService<?> productService = get(IProductService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return productService.findProductTypeForProduct(session, productType, system, identityToken)
                .chain(productItemType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                                .chain(activeFlag -> {
                                    tableForClassification.setEnterpriseID(enterprise);
                                    tableForClassification.setValue(value);
                                    tableForClassification.setSystemID(system);
                                    tableForClassification.setClassificationID(classification);
                                    tableForClassification.setOriginalSourceSystemID(system.getId());
                                    tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                    tableForClassification.setActiveFlagID(activeFlag);
                                    configureProductTypeLinkValue(tableForClassification, (J) this, productItemType, classification, value, enterprise);
                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                    if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                                    return session.insert(tableForClassification)
                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                    .onFailure().recoverWithItem(0L))
                                            .replaceWith((IRelationshipValue<J, IProductType<?, ?>, ?>) tableForClassification);
                                })));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addOrReuseProductTypes(Mutiny.StatelessSession session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
        IProductService<?> productService = get(IProductService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
                .onItem().transformToUni(productItemType -> tableForClassification.builder(session)
                        .findLink((J) this, productItemType, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .onFailure(NoResultException.class)
                        .recoverWithUni(() -> (Uni) addProductTypes(session, productTypeName, value, finalClassificationName, system, identityToken))
                        .chain(result -> Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) result)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IProductType<?, ?>, ?>> addOrUpdateProductTypes(Mutiny.StatelessSession session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
        IProductService<?> productService = get(IProductService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
                .chain(productItemType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> tableForClassification.builder(session)
                                .findLink((J) this, productItemType, searchValue)
                                .inActiveRange()
                                .withClassification(finalClassificationName, system)
                                .inDateRange()
                                .canRead(system, identityToken)
                                .get()
                                .onFailure(NoResultException.class)
                                .recoverWithUni(() -> (Uni) addProductTypes(session, productTypeName, value, finalClassificationName, system, identityToken))
                                .chain(result -> {
                                    IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existingTable =
                                            (IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) result;
                                    if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
                                        return Uni.createFrom().item((IRelationshipValue<J, IProductType<?, ?>, ?>) existingTable);
                                    }
                                    IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                    ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                                    return flagService.getArchivedFlag(session, enterprise, identityToken)
                                            .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                            .chain(() -> {
                                                IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getProductTypeRelationshipClass());
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
                                                            newTableForClassification.setValue(value);
                                                            newTableForClassification.setEnterpriseID(enterprise);
                                                            configureProductTypeLinkValue(newTableForClassification, (J) this, productItemType, classification, value, enterprise);
                                                            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                                    (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                            return session.insert(newTableForClassification)
                                                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                            .onFailure().recoverWithItem(0L))
                                                                    .replaceWith((IRelationshipValue<J, IProductType<?, ?>, ?>) newTableForClassification);
                                                        });
                                            });
                                })));
    }

    // ---- Stateless SCD close mutations (expire / archive / remove) via full-row session.update ----

    /** Stateless variant of {@link #expireProductTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> expireProductTypes(Mutiny.StatelessSession session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeProductTypesStateless(session, productTypeName, classificationName, searchValue, value, 0, system, identityToken);
    }

    /** Stateless variant of {@link #archiveProductTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> archiveProductTypes(Mutiny.StatelessSession session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeProductTypesStateless(session, productTypeName, classificationName, searchValue, value, 1, system, identityToken);
    }

    /** Stateless variant of {@link #removeProductTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> removeProductTypes(Mutiny.StatelessSession session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        return closeProductTypesStateless(session, productTypeName, classificationName, searchValue, value, 2, system, identityToken);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Uni<Void> closeProductTypesStateless(Mutiny.StatelessSession session, String productTypeName, String classificationName, String searchValue, String value, int mode, ISystems<?, ?> system, UUID... identityToken) {
        IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> tableForClassification = get(getProductTypeRelationshipClass());
        IProductService<?> productService = get(IProductService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;

        return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
                .chain(productItemType -> tableForClassification.builder(session)
                        .findLink((J) this, productItemType, searchValue)
                        .inActiveRange()
                        .withClassification(finalClassificationName, system)
                        .inDateRange()
                        .canRead(system, identityToken)
                        .get()
                        .map(r -> (Object) r)
                        .onFailure(NoResultException.class)
                        .recoverWithItem((Object) null)
                        .chain(resultObj -> {
                            IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existing =
                                    (IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) resultObj;
                            if (existing == null || Strings.nullToEmpty(value).equals(existing.getValue())) {
                                return Uni.createFrom().voidItem();
                            }
                            existing.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                            if (mode == 0) {
                                return session.update(existing).replaceWithVoid();
                            }
                            Uni<? extends com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag<?, ?>> flagUni =
                                    (mode == 1) ? flagService.getArchivedFlag(session, enterprise, identityToken)
                                                : flagService.getDeletedFlag(session, enterprise, identityToken);
                            return flagUni.chain(flag -> {
                                existing.setActiveFlagID(flag);
                                return session.update(existing).replaceWithVoid();
                            });
                        }));
    }

    /**
     * Stateless variant of {@link #updateProductTypes(Mutiny.Session, String, String, String, String, ISystems, UUID...)}
     * — SCD retire+reinsert only when the link already exists (no-op if absent).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Void> updateProductTypes(Mutiny.StatelessSession session, String productTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken) {
        IProductService<?> productService = get(IProductService.class);
        IClassificationService<?> classificationService = get(IClassificationService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? DefaultClassifications.NoClassification.toString() : classificationName;
        final IEnterprise<?, ?> enterprise = system.getEnterprise();
        return productService.findProductTypeForProduct(session, productTypeName, system, identityToken)
                .chain(productItemType -> classificationService.find(session, finalClassificationName, system, identityToken)
                        .chain(classification -> get(getProductTypeRelationshipClass()).builder(session)
                                .findLink((J) this, productItemType, searchValue)
                                .inActiveRange()
                                .withClassification(finalClassificationName, system)
                                .inDateRange()
                                .canRead(system, identityToken)
                                .get()
                                .map(r -> (Object) r)
                                .onFailure(NoResultException.class)
                                .recoverWithItem((Object) null)
                                .chain(resultObj -> {
                                    IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> existing =
                                            (IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?>) resultObj;
                                    if (existing == null || Strings.nullToEmpty(value).equals(existing.getValue())) {
                                        return Uni.createFrom().voidItem();
                                    }
                                    return flagService.getArchivedFlag(session, enterprise, identityToken)
                                            .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existing, existing.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
                                            .chain(() -> {
                                                IWarehouseRelationshipTable<?, ?, J, IProductType<?, ?>, java.util.UUID, ?> newRow = get(getProductTypeRelationshipClass());
                                                newRow.setId(null);
                                                newRow.setSystemID(system);
                                                newRow.setOriginalSourceSystemID(system.getId());
                                                newRow.setOriginalSourceSystemUniqueID(existing.getId());
                                                newRow.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                newRow.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                newRow.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                                newRow.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                                return flagService.getActiveFlag(session, enterprise, identityToken).chain(activeFlag -> {
                                                    newRow.setActiveFlagID(activeFlag);
                                                    newRow.setValue(value);
                                                    newRow.setEnterpriseID(enterprise);
                                                    configureProductTypeLinkValue(newRow, (J) this, productItemType, classification, value, enterprise);
                                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                            (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newRow;
                                                    return session.insert(newRow)
                                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                    .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                    .onFailure().recoverWithItem(0L))
                                                            .replaceWithVoid();
                                                });
                                            });
                                })));
    }
}





