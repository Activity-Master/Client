package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products.IProduct;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products.IProductType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.UUID;


/**
 * Service interface for managing products and product types.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IProductService<J extends IProductService<J>>
{
	/**
	 * The name of the Products system.
	 */
	String ProductSystemName = "Products System";

	/**
	 * Gets a new, uninitialized product instance.
	 *
	 * @return A new product instance
	 */
	IProduct<?,?> get();

	/**
	 * Finds a product by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param id      The UUID of the product
	 * @return A Uni emitting the found product
	 */
	Uni<IProduct<?,?>> find(Mutiny.Session session, UUID id);

	/**
	 * Finds a product type by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param id      The UUID of the product type
	 * @return A Uni emitting the found product type
	 */
	Uni<IProductType<?,?>> findType(Mutiny.Session session, UUID id);

	/**
	 * Gets a new, uninitialized product type instance.
	 *
	 * @return A new product type instance
	 */
	IProductType<?,?> getType();

	/**
	 * Creates a new product.
	 *
	 * @param session        The Mutiny session to use
	 * @param productType    The name of the product type
	 * @param name           The name of the product
	 * @param description    The description of the product
	 * @param code           The code for the product
	 * @param system         The system creating the product
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created product
	 */
	Uni<IProduct<?,?>> createProduct(Mutiny.Session session, String productType, String name, String description, String code, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new product with a specific key.
	 *
	 * @param session        The Mutiny session to use
	 * @param productType    The name of the product type
	 * @param key            The UUID key for the product
	 * @param name           The name of the product
	 * @param description    The description of the product
	 * @param code           The code for the product
	 * @param system         The system creating the product
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created product
	 */
	Uni<IProduct<?, ?>> createProduct(Mutiny.Session session, String productType, UUID key, String name, String description, String code, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Finds a product by name.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the product
	 * @param system         The system searching for the product
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found product
	 */
	Uni<IProduct<?,?>> findProduct(Mutiny.Session session, String name, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds a product associated with a specific resource item and classification.
	 *
	 * @param session            The Mutiny session to use
	 * @param resourceItem       The resource item
	 * @param classificationName The classification name
	 * @param value              The classification value
	 * @param system             The system searching for the product
	 * @param identityToken      Optional security identity tokens
	 * @return A Uni emitting a list of relationship values containing the product
	 */
	Uni<List<IRelationshipValue<IProduct<?,?>,IResourceItem<?,?>,?>>> findProductByResourceItem(Mutiny.Session session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Creates a new product type.
	 *
	 * @param session        The Mutiny session to use
	 * @param productType    The name of the product type
	 * @param description    The description
	 * @param system         The system creating the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created product type
	 */
	Uni<IProductType<?,?>> createProductType(Mutiny.Session session, String productType, String description, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new product type with a specific key.
	 *
	 * @param session        The Mutiny session to use
	 * @param productsType   The name of the product type
	 * @param key            The UUID key for the type
	 * @param description    The description
	 * @param system         The system creating the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created product type
	 */
	Uni<IProductType<?, ?>> createProductType(Mutiny.Session session, String productsType, UUID key, String description, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Finds the product type for a specific product type name.
	 *
	 * @param session        The Mutiny session to use
	 * @param productType    The name of the product type
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found product type
	 */
	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.Session session, String productType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds a product by name and classification data concept.
	 *
	 * @param session                           The Mutiny session to use
	 * @param productName                      The name of the product
	 * @param classificationDataConceptType    The data concept type classification
	 * @param system                            The system searching for the product
	 * @param identityToken                     Optional security identity tokens
	 * @return A Uni emitting the found product
	 */
	Uni<IProduct<?,?>> findProduct(Mutiny.Session session, String productName, IClassification<?,?> classificationDataConceptType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds the product type for a specific product and classification.
	 *
	 * @param session        The Mutiny session to use
	 * @param product        The product
	 * @param classification The classification
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found product type
	 */
	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.Session session, IProduct<?,?> product, IClassification<?,?> classification, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds the product type for a specific product and classification name.
	 *
	 * @param session        The Mutiny session to use
	 * @param product        The product
	 * @param classification The name of the classification
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found product type
	 */
	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.Session session, IProduct<?,?> product, String classification, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds product types by classification.
	 *
	 * @param session        The Mutiny session to use
	 * @param classification The classification
	 * @param system         The system searching for product types
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found product types
	 */
	Uni<List<IProductType<?,?>>> findProductTypes(Mutiny.Session session, IClassification<?,?> classification, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds product types by classification name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param classification The name of the classification
	 * @param system         The system searching for product types
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found product types
	 */
	Uni<List<IProductType<?,?>>> findProductTypes(Mutiny.Session session, String classification, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Finds products by product type.
	 *
	 * @param session        The Mutiny session to use
	 * @param type           The product type
	 * @param system         The system searching for products
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found products
	 */
	Uni<List<IProduct<?,?>>> findByProductTypes(Mutiny.Session session, IProductType<?,?> type, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds products by product type name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param type           The name of the product type
	 * @param system         The system searching for products
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found products
	 */
	Uni<List<IProduct<?,?>>> findByProductTypes(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);
}
