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


public interface IProductService<J extends IProductService<J>>
{
	String ProductSystemName = "Products System";

	IProduct<?,?> get();

	Uni<IProduct<?,?>> find(Mutiny.Session session, UUID id);

	Uni<IProductType<?,?>> findType(Mutiny.Session session, UUID id);

	IProductType<?,?> getType();

	Uni<IProduct<?,?>> createProduct(Mutiny.Session session, String productType, String name, String description, String code, ISystems<?,?> system, UUID... identityToken);

	Uni<IProduct<?, ?>> createProduct(Mutiny.Session session, String productType, UUID key, String name, String description, String code, ISystems<?, ?> system, UUID... identityToken);

	Uni<IProduct<?,?>> findProduct(Mutiny.Session session, String name, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRelationshipValue<IProduct<?,?>,IResourceItem<?,?>,?>>> findProductByResourceItem(Mutiny.Session session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

	Uni<IProductType<?,?>> createProductType(Mutiny.Session session, String productType, String description, ISystems<?,?> system, UUID... identityToken);

	Uni<IProductType<?, ?>> createProductType(Mutiny.Session session, String productsType, UUID key, String description, ISystems<?, ?> system, UUID... identityToken);

	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.Session session, String productType, ISystems<?,?> system, UUID... identityToken);

	Uni<IProduct<?,?>> findProduct(Mutiny.Session session, String productName, IClassification<?,?> classificationDataConceptType, ISystems<?,?> system, UUID... identityToken);

	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.Session session, IProduct<?,?> product, IClassification<?,?> classification, ISystems<?,?> system, UUID... identityToken);

	Uni<IProductType<?,?>> findProductTypeForProduct(Mutiny.Session session, IProduct<?,?> product, String classification, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IProductType<?,?>>> findProductTypes(Mutiny.Session session, IClassification<?,?> classification, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IProductType<?,?>>> findProductTypes(Mutiny.Session session, String classification, ISystems<?, ?> system, UUID... identityToken);

	Uni<List<IProduct<?,?>>> findByProductTypes(Mutiny.Session session, IProductType<?,?> type, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IProduct<?,?>>> findByProductTypes(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);
}
