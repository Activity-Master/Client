package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageResourceItemTypes;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


/**
 * Warehouse table interface for Resource Item entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IResourceItem<J extends IResourceItem<J, Q>,
		Q extends IResourceItemQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IContainsEnterprise<J>,
		        IContainsActiveFlags<J>,
		        IContainsSystem<J>,
		        IContainsData<J>,
		        IContainsHierarchy<J,java.util.UUID>,
		        IManageClassifications<J>,
		        IManageResourceItemTypes<J>
{

	/**
	 * Retrieves the filename associated with this resource item.
	 *
	 * @param session The reactive session
	 * @return A Uni containing the filename
	 */
	Uni<String> getFilename(Mutiny.Session session);

	/**
	 * Retrieves the data row for this resource item.
	 *
	 * @param session       The reactive session
	 * @param identityToken Security tokens
	 * @return A Uni containing the resource data
	 */
	Uni<IResourceData<?,?,?>> getDataRow(Mutiny.Session session, UUID... identityToken);

	/**
	 * Returns the data type of the resource item.
	 *
	 * @return A Uni containing the data type
	 */
	Uni<String> getResourceItemDataType();

	// ─────────────────────────────────────────────────────────────────────────
	// Fluent JSON document API (for JSON-typed resource items backed by MongoDB)
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Stores (creates or updates) a single field on this resource item's JSON document. The field path may use
	 * dot-notation for nested children (e.g. {@code "owner.name"}); the value may be a {@code String},
	 * {@code Integer}/number, {@code Boolean}, {@code List}/array, {@code Map}/POJO, or a Vert.x
	 * {@link io.vertx.core.json.JsonObject}/{@code JsonArray}. No-op unless this is a JSON-typed item and
	 * MongoDB is configured.
	 *
	 * @param fieldPath the (possibly nested) field path
	 * @param value     the value to store (any JSON-compatible type)
	 * @return a Uni completing when the field has been stored
	 */
	default Uni<Void> storeField(String fieldPath, Object value) {
		return Uni.createFrom().voidItem();
	}

	/**
	 * Stores (creates or updates) multiple fields on this resource item's JSON document in one operation.
	 *
	 * @param fields the field path → value map (dot-notation supported; values may be any JSON-compatible type)
	 * @return a Uni completing when the fields have been stored
	 */
	default Uni<Void> storeFields(java.util.Map<String, ?> fields) {
		return Uni.createFrom().voidItem();
	}

	/**
	 * Removes a field (or nested child) from this resource item's JSON document.
	 *
	 * @param fieldPath the (possibly nested) field path to remove
	 * @return a Uni completing when the field has been removed
	 */
	default Uni<Void> removeField(String fieldPath) {
		return Uni.createFrom().voidItem();
	}

	/**
	 * Appends a child to an array field on this resource item's JSON document.
	 *
	 * @param arrayPath the (possibly nested) array field path
	 * @param child     the child to append (object, list, scalar, …)
	 * @return a Uni completing when the child has been appended
	 */
	default Uni<Void> addJsonChild(String arrayPath, Object child) {
		return Uni.createFrom().voidItem();
	}

	/**
	 * Removes every child matching the criterion from an array field on this resource item's JSON document.
	 *
	 * @param arrayPath the (possibly nested) array field path
	 * @param match     the value/criterion identifying the children to remove
	 * @return a Uni completing when matching children have been removed
	 */
	default Uni<Void> removeJsonChild(String arrayPath, Object match) {
		return Uni.createFrom().voidItem();
	}

	/**
	 * Reads this resource item's JSON document.
	 *
	 * @return a Uni emitting the JSON document, or {@code null} when there is none / MongoDB is not configured
	 */
	default Uni<io.vertx.core.json.JsonObject> getJson() {
		return Uni.createFrom().nullItem();
	}

}
