package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsHierarchy;

import java.io.Serializable;
import java.util.UUID;


/**
 * Warehouse table interface for Event entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IEvent<J extends IEvent<J, Q>, Q extends IEventQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IManageEventTypes<J>,
		        IManageAddresses<J>,
		        IManageArrangements<J>,
		        IManageClassifications<J>,
		        IManageGeographies<J>,
		        IManageInvolvedParties<J>,
		        IManageProducts<J>,
		        IManageResourceItems<J>,
		        IManageRules<J>,
		        IContainsHierarchy<J,java.util.UUID>
{

}
