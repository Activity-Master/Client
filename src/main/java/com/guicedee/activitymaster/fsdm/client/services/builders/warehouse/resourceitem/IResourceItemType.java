package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseNameAndDescriptionTable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Interface for Resource Item Type entities in the warehouse.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IResourceItemType<J extends IResourceItemType<J, Q>,
		Q extends IResourceItemTypeQueryBuilder<Q, J>>
		extends IWarehouseNameAndDescriptionTable<J,Q, UUID>
{
}
