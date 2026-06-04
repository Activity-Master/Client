package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsHierarchy;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.io.Serializable;
import java.util.UUID;

/**
 * Warehouse table interface for Rules entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IRules<J extends IRules<J, Q>,
		Q extends IRulesQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IContainsNameAndDescription<J>,
		        IContainsHierarchy<J,java.util.UUID>,
		        IManageClassifications<J>,
		        IManageRuleTypes<J>,
		        IManageArrangements<J>,
		        IManageProducts<J>,
		        IManageResourceItems<J>
{

}
