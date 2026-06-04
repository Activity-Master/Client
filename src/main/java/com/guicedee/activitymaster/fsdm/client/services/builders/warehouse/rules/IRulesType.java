package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageResourceItems;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.io.Serializable;
import java.util.UUID;


/**
 * Interface for Rules Type entities in the warehouse.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IRulesType<J extends IRulesType<J, Q>,
		Q extends IRuleTypeQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IManageClassifications<J>,
		        IManageResourceItems<J>,
		        IContainsNameAndDescription<J>
{

}
