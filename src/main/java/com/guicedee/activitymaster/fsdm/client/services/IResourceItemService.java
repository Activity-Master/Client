package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IResourceItemService<J extends IResourceItemService<J>>
{
	String ResourceItemSystemName = "Resource Items System";

	IResourceItem<?,?> get();
	IResourceData<?,?,?> getData();

	default Uni<IResourceItemType<?,?>> createType(Mutiny.Session session, Enum<?> value, ISystems<?,?> system, UUID... identityToken)
	{
		return createType(session, value.toString(), value.toString(), system, identityToken);
	}

	default Uni<IResourceItemType<?,?>> createType(Mutiny.Session session, Enum<?> value, String description, ISystems<?,?> system, UUID... identityToken)
	{
		return createType(session, value.toString(), description, system, identityToken);
	}

	IResourceItemType<?,?> getType();

	Uni<IResourceItemType<?,?>> createType(Mutiny.Session session, String value, String description, ISystems<?,?> system, UUID... identityToken);

	Uni<IResourceItemType<?, ?>> createType(Mutiny.Session session, String value, UUID key, String description, ISystems<?, ?> system, UUID... identityToken);

	Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue,
									ISystems<?,?> system, UUID... identityToken);

	Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue, byte[] data,
									ISystems<?, ?> system, UUID... identityToken);

	Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue,
									ISystems<?, ?> system, UUID... identityToken);

	Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue, byte[] data,
									ISystems<?, ?> system, UUID... identityToken);

	Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
									LocalDateTime effectiveFromDate,
									ISystems<?,?> system, UUID... identityToken);

	Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
									LocalDateTime effectiveFromDate, byte[] data,
									ISystems<?, ?> system, UUID... identityToken);

	Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
									LocalDateTime effectiveFromDate,
									ISystems<?, ?> system, UUID... identityToken);

    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
									LocalDateTime effectiveFromDate, byte[] data,
									ISystems<?, ?> system, UUID... identityToken);
		
		/**
			* Direct method for updating only the data (not any metadata or last updated)
			*
			* @param session
			* @param data
			* @param resourceItemId
			* @return
			*/

		Uni<Void> updateResourceData(Mutiny.Session session, byte[] data, UUID resourceItemId);

  Uni<Void> addResourceItemTypeRelationship(Mutiny.Session session, IResourceItem<?,?> resourceItem, String typeName, String value, ISystems<?, ?> system, UUID... identityToken);

  Uni<IResourceItem<?,?>> findByClassification(Mutiny.Session session, String resourceType,
                                               String classification,
                                               String value,
                                               ISystems<?,?> systems,
                                               UUID... identityToken);

	Uni<List<IRelationshipValue<IResourceItem<?, ?>, IClassification<?,?>,?>>> findByClassificationAll(Mutiny.Session session, String resourceType,
																									   String classification,
																									   String value,
																									   ISystems<?, ?> systems,
																									   UUID... identityToken);

	Uni<IResourceItem<?,?>> findByUUID(Mutiny.Session session, UUID uuid);

	Uni<IResourceItem<?,?>> findByOriginalSourceUniqueID(Mutiny.Session session, UUID originalSourceUniqueID,
														 ISystems<?,?> systems,
														 UUID... identityToken);

	Uni<IResourceItemType<?,?>> findResourceItemType(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IResourceItem<?,?>>> findByResourceItemType(Mutiny.Session session, String type, ISystems<?,?> systems, UUID... identityToken);

	Uni<List<IResourceItem<?,?>>> findByResourceItemType(Mutiny.Session session, String type, String value, ISystems<?,?> systems, UUID... identityToken);

 	Uni<IResourceItem<?, ?>> createAndFind(Mutiny.Session session, String identityResourceType, String resourceItemDataValue,
	                                        ISystems<?, ?> system, UUID... identityToken);

    /**
     * Resolve ResourceItemType ID (UUID) by enterprise and name, using cache and ActiveFlag visible range with SCD window.
     * Contract: never returns null; lets NoResultException propagate on misses.
     */
    default Uni<java.util.UUID> resolveResourceItemTypeIdByName(Mutiny.Session session, java.util.UUID enterpriseId, String resourceItemTypeName) {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getResourceItemTypeId(session, enterpriseId, resourceItemTypeName, (sess, name) -> {
                    var afService = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService.class);
                    return afService.getVisibleRangeAndUpIds(sess, enterpriseId)
                            .flatMap(visibleIds -> {
                                String sql = "select resourceitemtypeid from resource.resourceitemtype " +
                                        "where enterpriseid = :ent and resourceitemtypename = :name " +
                                        "and (effectivefromdate <= current_timestamp) " +
                                        "and (effectivetodate > current_timestamp) " +
                                        "and activeflagid in (:visibleIds)";
                                return sess.createNativeQuery(sql)
                                        .setParameter("ent", enterpriseId)
                                        .setParameter("name", name)
                                        .setParameter("visibleIds", visibleIds)
                                        .getSingleResult()
                                        .map(result -> (java.util.UUID) result);
                            });
                });
    }
}
