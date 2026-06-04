package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.io.Serializable;
import java.util.UUID;


/**
 * Interface for Product Type entities in the warehouse.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IProductType<J extends IProductType<J, Q>,
		Q extends IProductTypeQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IManageClassifications<J>,
		        IContainsNameAndDescription<J>
{

}
