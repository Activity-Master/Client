package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseNameAndDescriptionTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Warehouse table interface for Product entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IProduct<J extends IProduct<J,Q>, Q extends IProductQueryBuilder<Q,J>>
		extends IWarehouseNameAndDescriptionTable<J, Q, UUID>,
		        IManageClassifications<J>,
		        IManageResourceItems<J>,
		        IManageProductTypes<J>
{

}
