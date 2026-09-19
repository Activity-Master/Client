package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Service interface for managing resource items.
 * Resource items are atomic pieces of information or files stored within the system.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IResourceItemService<J extends IResourceItemService<J>> {
    /**
     * The name of the Resource Items system.
     */
    String ResourceItemSystemName = "Resource Items System";

    /**
     * Gets a new, uninitialized resource item instance.
     *
     * @return A new resource item instance
     */
    IResourceItem<?, ?> get();

    /**
     * Gets a new, uninitialized resource data instance.
     *
     * @return A new resource data instance
     */
    IResourceData<?, ?, ?> getData();

    /**
     * Gets a new, uninitialized resource item type instance.
     *
     * @return A new resource item type instance
     */
    IResourceItemType<?, ?> getType();

    /**
     * Stateless variant of {@link #createType(Mutiny.StatelessSession, String, String, ISystems, UUID...)} — a
     * find-or-create resource-item type provisioned entirely on a {@link Mutiny.StatelessSession}
     * (scalar existence/prep + stateless insert + stateless default security).
     */
    Uni<IResourceItemType<?, ?>> createType(Mutiny.StatelessSession session, String value, String description, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #createType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
    default Uni<IResourceItemType<?, ?>> createType(Mutiny.StatelessSession session, String value, ISystems<?, ?> system, UUID... identityToken) {
        return createType(session, value, value, system, identityToken);
    }

    /** Stateless variant of {@link #createType(Mutiny.StatelessSession, Enum, ISystems, UUID...)}. */
    default Uni<IResourceItemType<?, ?>> createType(Mutiny.StatelessSession session, Enum<?> value, ISystems<?, ?> system, UUID... identityToken) {
        return createType(session, value.toString(), value.toString(), system, identityToken);
    }

    /** Stateless variant of {@link #createType(Mutiny.StatelessSession, Enum, String, ISystems, UUID...)}. */
    default Uni<IResourceItemType<?, ?>> createType(Mutiny.StatelessSession session, Enum<?> value, String description, ISystems<?, ?> system, UUID... identityToken) {
        return createType(session, value.toString(), description, system, identityToken);
    }

    /** Stateless variant of {@link #createType(Mutiny.StatelessSession, String, UUID, String, ISystems, UUID...)}. */
    Uni<IResourceItemType<?, ?>> createType(Mutiny.StatelessSession session, String value, UUID key, String description, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless scope-restricted variant of {@link #createTypeScopeRestricted(Mutiny.StatelessSession, String, UUID, String, ISystems, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, UUID...)}. */
    Uni<IResourceItemType<?, ?>> createTypeScopeRestricted(Mutiny.StatelessSession session, String value, UUID key, String description, ISystems<?, ?> system,
                                                           com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
                                                           UUID... identityToken);

    /** Stateless variant of {@link #updateResourceData(Mutiny.StatelessSession, byte[], UUID, String)}. */
    Uni<Void> updateResourceData(Mutiny.StatelessSession session, byte[] data, UUID resourceItemId, String systemName);

    /** Stateless variant of {@link #updateResourceData(Mutiny.StatelessSession, byte[], UUID)} (relational only). */
    Uni<Void> updateResourceData(Mutiny.StatelessSession session, byte[] data, UUID resourceItemId);

    /** Stateless find-or-insert variant of {@link #create(Mutiny.StatelessSession, String, String, byte[], ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, String resourceItemDataValue, byte[] data,
                                    ISystems<?, ?> system, UUID... identityToken);

    // --- Full stateless create family: a stateless twin for every managed create overload so a managed
    // create call converts to stateless by only changing the session type. Each links the resource-item
    // TYPE relationship and honours key / originalSourceSystemUniqueID / effectiveFromDate. ---

    /** Stateless variant of {@link #create(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, String resourceItemDataValue,
                                    ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #create(Mutiny.StatelessSession, String, UUID, String, ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, UUID key, String resourceItemDataValue,
                                    ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #create(Mutiny.StatelessSession, String, UUID, String, byte[], ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, UUID key, String resourceItemDataValue, byte[] data,
                                    ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #create(Mutiny.StatelessSession, String, String, UUID, LocalDateTime, ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #create(Mutiny.StatelessSession, String, String, UUID, LocalDateTime, byte[], ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate, byte[] data, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #create(Mutiny.StatelessSession, String, UUID, String, UUID, LocalDateTime, ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, UUID key, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #create(Mutiny.StatelessSession, String, UUID, String, UUID, LocalDateTime, byte[], ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> create(Mutiny.StatelessSession session, String identityResourceType, UUID key, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate, byte[] data, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless scope-restricted variant of the stateless {@link #create(Mutiny.StatelessSession, String, String, byte[], ISystems, UUID...)} — secures the resource item with the restricted matrix plus a read grant for {@code scopeToken}. */
    Uni<IResourceItem<?, ?>> createScopeRestricted(Mutiny.StatelessSession session, String identityResourceType, String resourceItemDataValue, byte[] data,
                                    ISystems<?, ?> system,
                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
                                    UUID... identityToken);

    /** Stateless scope-restricted variant of {@link #createScopeRestricted(Mutiny.StatelessSession, String, UUID, String, UUID, LocalDateTime, byte[], ISystems, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, UUID...)}. */
    Uni<IResourceItem<?, ?>> createScopeRestricted(Mutiny.StatelessSession session, String identityResourceType, UUID key, String resourceItemDataValue,
                                    UUID originalSourceSystemUniqueID, LocalDateTime effectiveFromDate, byte[] data,
                                    ISystems<?, ?> system,
                                    com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
                                    UUID... identityToken);

    /** Stateless variant of {@link #findByUUID(Mutiny.StatelessSession, UUID)}. */
    Uni<IResourceItem<?, ?>> findByUUID(Mutiny.StatelessSession session, UUID uuid);

    /** Stateless variant of {@link #addResourceItemTypeRelationship(Mutiny.StatelessSession, IResourceItem, String, String, ISystems, UUID...)}. */
    Uni<Void> addResourceItemTypeRelationship(Mutiny.StatelessSession session, IResourceItem<?, ?> resourceItem, String typeName, String value, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findByClassification(Mutiny.StatelessSession, String, String, String, ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> findByClassification(Mutiny.StatelessSession session, String resourceType,
                                                  String classification,
                                                  String value,
                                                  ISystems<?, ?> systems,
                                                  UUID... identityToken);

    /** Stateless variant of {@link #findByClassificationAll(Mutiny.StatelessSession, String, String, String, ISystems, UUID...)}. */
    Uni<List<IRelationshipValue<IResourceItem<?, ?>, IClassification<?, ?>, ?>>> findByClassificationAll(Mutiny.StatelessSession session, String resourceType,
                                                                                                         String classification,
                                                                                                         String value,
                                                                                                         ISystems<?, ?> systems,
                                                                                                         UUID... identityToken);

    /** Stateless variant of {@link #findByOriginalSourceUniqueID(Mutiny.StatelessSession, UUID, ISystems, UUID...)}. */
    Uni<IResourceItem<?, ?>> findByOriginalSourceUniqueID(Mutiny.StatelessSession session, UUID originalSourceUniqueID,
                                                          ISystems<?, ?> systems,
                                                          UUID... identityToken);

    /** Stateless "fetch ids/scalars + prep" variant of {@link #findResourceItemType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
    Uni<IResourceItemType<?, ?>> findResourceItemType(Mutiny.StatelessSession session, String type, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findByResourceItemType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
    Uni<List<IResourceItem<?, ?>>> findByResourceItemType(Mutiny.StatelessSession session, String type, ISystems<?, ?> systems, UUID... identityToken);

    /** Stateless variant of {@link #findByResourceItemType(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
    Uni<List<IResourceItem<?, ?>>> findByResourceItemType(Mutiny.StatelessSession session, String type, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds JSON resource-item data documents in MongoDB using native query criteria.
     * <p>
     * Resource items whose type is a JSON type (e.g. {@code JsonPacket}) store their payload as a MongoDB
     * document keyed by the resource item id (see the core {@code ResourceItemJsonStore}). This method runs a
     * lookup over those documents. Returns an empty list when MongoDB is not configured for the deployment.
     *
     * @param query the MongoDB query criteria (a {@code null}/empty object matches all documents)
     * @return a Uni emitting the matching JSON documents
     */
    default Uni<List<io.vertx.core.json.JsonObject>> findJsonResourceData(io.vertx.core.json.JsonObject query) {
        return Uni.createFrom().item(java.util.Collections.emptyList());
    }

    /**
     * Fetches the JSON payload stored in MongoDB for a resource item.
     *
     * @param resourceItemId the resource item id
     * @return a Uni emitting the JSON payload, or {@code null} when absent / MongoDB is not configured
     */
    default Uni<io.vertx.core.json.JsonObject> getJsonResourceData(UUID resourceItemId) {
        return Uni.createFrom().nullItem();
    }

    /**
     * Finds JSON resource-item documents in a <strong>named</strong> MongoDB collection using native criteria.
     * Different JSON resource types can be routed to their own collections (see the core
     * {@code ResourceItemJsonStore} / {@code RESOURCE_ITEM_JSON_COLLECTIONS}).
     *
     * @param collection the MongoDB collection name
     * @param query      the MongoDB query criteria (a {@code null}/empty object matches all documents)
     * @return a Uni emitting the matching documents
     */
    default Uni<List<io.vertx.core.json.JsonObject>> findJsonResourceData(String collection, io.vertx.core.json.JsonObject query) {
        return Uni.createFrom().item(java.util.Collections.emptyList());
    }

    /**
     * Sets a single field on a resource item's JSON document ({@code $set}); the field path may use
     * dot-notation to reach nested children (e.g. {@code "address.city"}).
     *
     * @param resourceItemId the resource item id
     * @param fieldPath      the (possibly nested) field path
     * @param value          the value to set
     * @return a Uni completing when the field has been updated (no-op when MongoDB is not configured)
     */
    default Uni<Void> updateJsonResourceField(UUID resourceItemId, String fieldPath, Object value) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Removes a field (or nested child) from a resource item's JSON document ({@code $unset}).
     *
     * @param resourceItemId the resource item id
     * @param fieldPath      the (possibly nested) field path to remove
     * @return a Uni completing when the field has been removed (no-op when MongoDB is not configured)
     */
    default Uni<Void> removeJsonResourceField(UUID resourceItemId, String fieldPath) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Appends a child to an array field on a resource item's JSON document ({@code $push}).
     *
     * @param resourceItemId the resource item id
     * @param arrayPath      the (possibly nested) array field path
     * @param child          the child document to append
     * @return a Uni completing when the child has been appended (no-op when MongoDB is not configured)
     */
    default Uni<Void> addJsonResourceChild(UUID resourceItemId, String arrayPath, io.vertx.core.json.JsonObject child) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Removes every child matching the criterion from an array field on a resource item's JSON document
     * ({@code $pull}).
     *
     * @param resourceItemId the resource item id
     * @param arrayPath      the (possibly nested) array field path
     * @param match          the value/criterion identifying the children to remove
     * @return a Uni completing when matching children have been removed (no-op when MongoDB is not configured)
     */
    default Uni<Void> removeJsonResourceChild(UUID resourceItemId, String arrayPath, Object match) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Stateless variant of {@link #resolveResourceItemTypeIdByName(Mutiny.StatelessSession, IEnterprise, String)}.
     * <p>
     * Resolves the resource-item-type id via a scalar native-SQL lookup (never hydrating the
     * {@code @Cacheable} {@code ResourceItemType} entity), so it is safe on a {@link Mutiny.StatelessSession}.
     * Mirrors the managed contract exactly (SCD window compared against the logical {@code now}, ActiveFlag
     * visible-range filter via the stateless {@code getVisibleRangeAndUpIds}) and shares the same
     * {@link com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache} key space.
     */
    default Uni<java.util.UUID> resolveResourceItemTypeIdByName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterpriseId, String resourceItemTypeName) {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getResourceItemTypeId(session, enterpriseId.getId(), resourceItemTypeName, (sess, name) -> {
                    var afService = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService.class);
                    java.time.OffsetDateTime now = com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD
                            .convertToUTCDateTime(com.entityassist.RootEntity.getNow());
                    return afService.getVisibleRangeAndUpIds(sess, enterpriseId)
                            .flatMap(visibleIds -> {
                                String sql = "select resourceitemtypeid from resource.resourceitemtype " +
                                        "where enterpriseid = :ent and resourceitemtypename = :name " +
                                        "and (effectivefromdate <= :now) " +
                                        "and (effectivetodate > :now) " +
                                        "and activeflagid in (:visibleIds)";
                                return sess.createNativeQuery(sql, java.util.UUID.class)
                                        .setParameter("ent", enterpriseId.getId())
                                        .setParameter("name", name)
                                        .setParameter("now", now)
                                        .setParameter("visibleIds", visibleIds)
                                        .getSingleResult();
                            });
                });
    }
}
