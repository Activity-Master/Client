package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsHierarchy;

import java.io.Serializable;
import java.util.UUID;

/**
 * Warehouse table interface for Arrangement entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IArrangement<J extends IArrangement<J, Q>,
		Q extends IArrangementQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IManageArrangementTypes<J>,
		        IManageClassifications<J>,
		        IManageRules<J>,
		        IManageRuleTypes<J>,
		        IManageProducts<J>,
		        IContainsHierarchy<J,java.util.UUID>,
		        IManageResourceItems<J>,
		        IManageInvolvedParties<J>
{
}
