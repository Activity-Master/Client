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
     * Creates a new resource item type using an enum.
     *
     * @param session        The Mutiny session to use
     * @param value           The type enum
     * @param system          The system creating the type
     * @param identityToken   Optional security identity tokens
     * @return A Uni emitting the created resource item type
     */
    default Uni<IResourceItemType<?, ?>> createType(Mutiny.Session session, Enum<?> value, ISystems<?, ?> system, UUID... identityToken) {
        return createType(session, value.toString(), value.toString(), system, identityToken);
    }

    /**
     * Creates a new resource item type using an enum and a description.
     *
     * @param session        The Mutiny session to use
     * @param value           The type enum
     * @param description     The description
     * @param system          The system creating the type
     * @param identityToken   Optional security identity tokens
     * @return A Uni emitting the created resource item type
     */
    default Uni<IResourceItemType<?, ?>> createType(Mutiny.Session session, Enum<?> value, String description, ISystems<?, ?> system, UUID... identityToken) {
        return createType(session, value.toString(), description, system, identityToken);
    }

    /**
     * Gets a new, uninitialized resource item type instance.
     *
     * @return A new resource item type instance
     */
    IResourceItemType<?, ?> getType();

    /**
     * Creates a new resource item type by name and description.
     *
     * @param session        The Mutiny session to use
     * @param value           The name of the type
     * @param description     The description
     * @param system          The system creating the type
     * @param identityToken   Optional security identity tokens
     * @return A Uni emitting the created resource item type
     */
    Uni<IResourceItemType<?, ?>> createType(Mutiny.Session session, String value, String description, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item type with a specific key.
     *
     * @param session        The Mutiny session to use
     * @param value           The name of the type
     * @param key             The UUID key for the type
     * @param description     The description
     * @param system          The system creating the type
     * @param identityToken   Optional security identity tokens
     * @return A Uni emitting the created resource item type
     */
    Uni<IResourceItemType<?, ?>> createType(Mutiny.Session session, String value, UUID key, String description, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Opt-in <strong>scope-restricted</strong> resource-item-type create. Same as
     * {@link #createType(Mutiny.Session, String, UUID, String, ISystems, UUID...)} but secured with the restricted
     * matrix plus a <em>read</em> grant for {@code scopeToken}.
     *
     * @param session        The Mutiny session to use
     * @param value          The name of the type
     * @param key            The UUID key for the type, or {@code null} to generate one
     * @param description    The description
     * @param system         The system creating the type
     * @param scopeToken     The scope token granted read on the new resource item type
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created (scope-restricted) resource item type
     */
    Uni<IResourceItemType<?, ?>> createTypeScopeRestricted(Mutiny.Session session, String value, UUID key, String description, ISystems<?, ?> system,
                                                           com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
                                                           UUID... identityToken);

    /**
     * Creates a new resource item.
     *
     * @param session               The Mutiny session to use
     * @param identityResourceType  The name of the resource item type
     * @param resourceItemDataValue The text value for the resource item
     * @param system                The system creating the item
     * @param identityToken         Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item with binary data.
     *
     * @param session               The Mutiny session to use
     * @param identityResourceType  The name of the resource item type
     * @param resourceItemDataValue The text value for the resource item
     * @param data                  The binary data
     * @param system                The system creating the item
     * @param identityToken         Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue, byte[] data,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item with a specific key.
     *
     * @param session               The Mutiny session to use
     * @param identityResourceType  The name of the resource item type
     * @param key                   The UUID key for the item
     * @param resourceItemDataValue The text value for the resource item
     * @param system                The system creating the item
     * @param identityToken         Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item with a specific key and binary data.
     *
     * @param session               The Mutiny session to use
     * @param identityResourceType  The name of the resource item type
     * @param key                   The UUID key for the item
     * @param resourceItemDataValue The text value for the resource item
     * @param data                  The binary data
     * @param system                The system creating the item
     * @param identityToken         Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue, byte[] data,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item with source system ID and effective date.
     *
     * @param session                     The Mutiny session to use
     * @param identityResourceType        The name of the resource item type
     * @param resourceItemDataValue       The text value for the resource item
     * @param originalSourceSystemUniqueID The ID in the source system
     * @param effectiveFromDate            The effective date
     * @param system                      The system creating the item
     * @param identityToken               Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item with source system ID, effective date, and binary data.
     *
     * @param session                     The Mutiny session to use
     * @param identityResourceType        The name of the resource item type
     * @param resourceItemDataValue       The text value for the resource item
     * @param originalSourceSystemUniqueID The ID in the source system
     * @param effectiveFromDate            The effective date
     * @param data                        The binary data
     * @param system                      The system creating the item
     * @param identityToken               Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate, byte[] data,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item with key, source system ID, and effective date.
     *
     * @param session                     The Mutiny session to use
     * @param identityResourceType        The name of the resource item type
     * @param key                         The UUID key for the item
     * @param resourceItemDataValue       The text value for the resource item
     * @param originalSourceSystemUniqueID The ID in the source system
     * @param effectiveFromDate            The effective date
     * @param system                      The system creating the item
     * @param identityToken               Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new resource item with key, source system ID, effective date, and binary data.
     *
     * @param session                     The Mutiny session to use
     * @param identityResourceType        The name of the resource item type
     * @param key                         The UUID key for the item
     * @param resourceItemDataValue       The text value for the resource item
     * @param originalSourceSystemUniqueID The ID in the source system
     * @param effectiveFromDate            The effective date
     * @param data                        The binary data
     * @param system                      The system creating the item
     * @param identityToken               Optional security identity tokens
     * @return A Uni emitting the created resource item
     */
    Uni<IResourceItem<?, ?>> create(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                    LocalDateTime effectiveFromDate, byte[] data,
                                    ISystems<?, ?> system, UUID... identityToken);

    /**
     * Opt-in <strong>scope-restricted</strong> resource-item create. Identical to
     * {@link #create(Mutiny.Session, String, UUID, String, UUID, LocalDateTime, byte[], ISystems, UUID...)} except
     * the resource item's data row and its type relationship are secured with the restricted matrix: only
     * Administrators / Systems / Applications / Plugins retain access, plus a <em>read</em> grant for
     * {@code scopeToken}. Because the applicable-token climb is child&rarr;parent, only identity tokens located at
     * the {@code scopeToken} node <em>or below it</em> may read.
     *
     * @param session                     The Mutiny session to use
     * @param identityResourceType        The resource type identity
     * @param key                         The UUID key for the resource item
     * @param resourceItemDataValue       The resource item data value
     * @param originalSourceSystemUniqueID The original source system unique ID
     * @param effectiveFromDate           The effective-from date
     * @param data                        The binary data
     * @param system                      The system creating the item
     * @param scopeToken                  The scope token granted read on the new resource item
     * @param identityToken               Optional security identity tokens
     * @return A Uni emitting the created (scope-restricted) resource item
     */
    Uni<IResourceItem<?, ?>> createScopeRestricted(Mutiny.Session session, String identityResourceType, UUID key, String resourceItemDataValue, UUID originalSourceSystemUniqueID,
                                                   LocalDateTime effectiveFromDate, byte[] data,
                                                   ISystems<?, ?> system,
                                                   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
                                                   UUID... identityToken);

    /**
     * Direct method for updating only the binary data of a resource item.
     *
     * @param session        The Mutiny session to use
     * @param data           The new binary data
     * @param resourceItemId The UUID of the resource item
     * @return A Uni that completes when the data is updated
     */
    default Uni<Void> updateResourceData(Mutiny.Session session, byte[] data, UUID resourceItemId) {
        return updateResourceData(session, data, resourceItemId, null);
    }

    /**
     * Direct method for updating only the data (not any metadata or last updated).
     * Accepts an optional systemName used when auto-creating missing resource data.
     *
     * @param session        The Mutiny session to use
     * @param data           The new binary data
     * @param resourceItemId The UUID of the resource item
     * @param systemName     The requesting system name (nullable — falls back to ActivityMasterSystemName)
     * @return A Uni that completes when the data is updated
     */
    Uni<Void> updateResourceData(Mutiny.Session session, byte[] data, UUID resourceItemId, String systemName);

    /**
     * Adds a relationship between a resource item and a type.
     *
     * @param session        The Mutiny session to use
     * @param resourceItem   The resource item
     * @param typeName       The name of the relationship type
     * @param value          The value for the relationship
     * @param system         The system performing the operation
     * @param identityToken  Optional security identity tokens
     * @return A Uni that completes when the relationship is added
     */
    Uni<Void> addResourceItemTypeRelationship(Mutiny.Session session, IResourceItem<?, ?> resourceItem, String typeName, String value, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds a resource item by its type and classification.
     *
     * @param session        The Mutiny session to use
     * @param resourceType   The resource item type name
     * @param classification The classification name
     * @param value          The classification value
     * @param systems        The system searching for the item
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the found resource item
     */
    Uni<IResourceItem<?, ?>> findByClassification(Mutiny.Session session, String resourceType,
                                                  String classification,
                                                  String value,
                                                  ISystems<?, ?> systems,
                                                  UUID... identityToken);

    /**
     * Finds all resource items matching a type and classification.
     *
     * @param session        The Mutiny session to use
     * @param resourceType   The resource item type name
     * @param classification The classification name
     * @param value          The classification value
     * @param systems        The system searching for items
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting a list of relationship values containing resource items
     */
    Uni<List<IRelationshipValue<IResourceItem<?, ?>, IClassification<?, ?>, ?>>> findByClassificationAll(Mutiny.Session session, String resourceType,
                                                                                                         String classification,
                                                                                                         String value,
                                                                                                         ISystems<?, ?> systems,
                                                                                                         UUID... identityToken);

    /**
     * Finds a resource item by its unique ID.
     *
     * @param session The Mutiny session to use
     * @param uuid    The UUID of the resource item
     * @return A Uni emitting the found resource item
     */
    Uni<IResourceItem<?, ?>> findByUUID(Mutiny.Session session, UUID uuid);

    /**
     * Finds a resource item by its original source unique ID.
     *
     * @param session               The Mutiny session to use
     * @param originalSourceUniqueID The ID in the source system
     * @param systems               The system searching for the item
     * @param identityToken          Optional security identity tokens
     * @return A Uni emitting the found resource item
     */
    Uni<IResourceItem<?, ?>> findByOriginalSourceUniqueID(Mutiny.Session session, UUID originalSourceUniqueID,
                                                          ISystems<?, ?> systems,
                                                          UUID... identityToken);

    /**
     * Finds a resource item type by name.
     *
     * @param session        The Mutiny session to use
     * @param type           The name of the type
     * @param system         The system searching for the type
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the found resource item type
     */
    Uni<IResourceItemType<?, ?>> findResourceItemType(Mutiny.Session session, String type, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds all resource items of a given type.
     *
     * @param session        The Mutiny session to use
     * @param type           The name of the resource item type
     * @param systems        The system searching for items
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting a list of resource items
     */
    Uni<List<IResourceItem<?, ?>>> findByResourceItemType(Mutiny.Session session, String type, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds resource items of a given type with a specific value.
     *
     * @param session        The Mutiny session to use
     * @param type           The name of the resource item type
     * @param value          The text value to match
     * @param systems        The system searching for items
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting a list of resource items
     */
    Uni<List<IResourceItem<?, ?>>> findByResourceItemType(Mutiny.Session session, String type, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Resolve ResourceItemType ID (UUID) by enterprise and name, using cache and ActiveFlag visible range with SCD window.
     * Contract: never returns null; lets NoResultException propagate on misses.
     */
    default Uni<java.util.UUID> resolveResourceItemTypeIdByName(Mutiny.Session session, IEnterprise<?, ?> enterpriseId, String resourceItemTypeName) {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getResourceItemTypeId(session, enterpriseId.getId(), resourceItemTypeName, (sess, name) -> {
                    var afService = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService.class);
                    // Compare the SCD effective window against the same logical "now" the entities are written
                    // with (convertToUTCDateTime(RootEntity.getNow())) rather than the database current_timestamp.
                    // The native query does not auto-flush and the DB clock can trail the just-written effective
                    // date, so using current_timestamp made a row created earlier in this transaction invisible.
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
