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

	/** Stateless variant of {@link #find(Mutiny.StatelessSession, UUID)}. */
	Uni<IProduct<?,?>> find(Mutiny.StatelessSession session, UUID id);

	/** Stateless variant of {@link #findType(Mutiny.StatelessSession, UUID)}. */
	Uni<IProductType<?,?>> findType(Mutiny.StatelessSession session, UUID id);

	/**
	 * Gets a new, uninitialized product type instance.
	 *
	 * @return A new product type instance
	 */
	IProductType<?,?> getType();

	/** Stateless variant of {@link #findProduct(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IProduct<?,?>> findProduct(Mutiny.StatelessSession session, String name, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findProductByResourceItem(Mutiny.StatelessSession, IResourceItem, String, String, ISystems, UUID...)}. */
	Uni<List<IRelationshipValue<IProduct<?,?>,IResourceItem<?,?>,?>>> findProductByResourceItem(Mutiny.StatelessSession session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

	/** Stateless "fetch ids/scalars + prep" variant of {@link #findProductTypeForProduct(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.StatelessSession session, String productType, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findProduct(Mutiny.StatelessSession, String, IClassification, ISystems, UUID...)}. */
	Uni<IProduct<?,?>> findProduct(Mutiny.StatelessSession session, String productName, IClassification<?,?> classificationDataConceptType, ISystems<?,?> system, UUID... identityToken);

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
	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.StatelessSession session, IProduct<?,?> product, IClassification<?,?> classification, ISystems<?,?> system, UUID... identityToken);

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
	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.StatelessSession session, IProduct<?,?> product, String classification, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findProductTypes(Mutiny.StatelessSession, IClassification, ISystems, UUID...)}. */
	Uni<List<IProductType<?,?>>> findProductTypes(Mutiny.StatelessSession session, IClassification<?,?> classification, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findProductTypes(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<List<IProductType<?,?>>> findProductTypes(Mutiny.StatelessSession session, String classification, ISystems<?, ?> system, UUID... identityToken);

	/** Stateless variant of {@link #findByProductTypes(Mutiny.StatelessSession, IProductType, ISystems, UUID...)}. */
	Uni<List<IProduct<?,?>>> findByProductTypes(Mutiny.StatelessSession session, IProductType<?,?> type, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findByProductTypes(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<List<IProduct<?,?>>> findByProductTypes(Mutiny.StatelessSession session, String type, ISystems<?,?> system, UUID... identityToken);

	// ============================================================================================
	// Stateless create twins (Product + ProductType, public + scope-restricted). Each runs entirely on
	// the supplied Mutiny.StatelessSession (scalar existence gate + session.insert + stateless security
	// matrix), so independent stateless sessions can provision products in parallel.
	// ============================================================================================

	/** Stateless variant of {@link #createProduct(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
	Uni<IProduct<?,?>> createProduct(Mutiny.StatelessSession session, String productType, String name, String description, String code, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #createProduct(Mutiny.StatelessSession, String, UUID, String, String, String, ISystems, UUID...)}. */
	Uni<IProduct<?,?>> createProduct(Mutiny.StatelessSession session, String productType, UUID key, String name, String description, String code, ISystems<?,?> system, UUID... identityToken);

	/** Stateless scope-restricted variant of {@link #createProductScopeRestricted(Mutiny.StatelessSession, String, UUID, String, String, String, ISystems, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, UUID...)}. */
	Uni<IProduct<?,?>> createProductScopeRestricted(Mutiny.StatelessSession session, String productType, UUID key, String name, String description, String code, ISystems<?,?> system,
													com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?,?> scopeToken,
													UUID... identityToken);

	/** Stateless variant of {@link #createProductType(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	Uni<IProductType<?,?>> createProductType(Mutiny.StatelessSession session, String productType, String description, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #createProductType(Mutiny.StatelessSession, String, UUID, String, ISystems, UUID...)}. */
	Uni<IProductType<?,?>> createProductType(Mutiny.StatelessSession session, String productsType, UUID key, String description, ISystems<?,?> system, UUID... identityToken);

	/** Stateless scope-restricted variant of {@link #createProductTypeScopeRestricted(Mutiny.StatelessSession, String, UUID, String, ISystems, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, UUID...)}. */
	Uni<IProductType<?,?>> createProductTypeScopeRestricted(Mutiny.StatelessSession session, String productsType, UUID key, String description, ISystems<?,?> system,
														   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?,?> scopeToken,
														   UUID... identityToken);
}
