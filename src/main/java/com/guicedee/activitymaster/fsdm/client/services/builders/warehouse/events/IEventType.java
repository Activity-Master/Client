package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.io.Serializable;
import java.util.UUID;

public interface IEventType<J extends IEventType<J, Q>,
		Q extends IEventTypeQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IContainsNameAndDescription<J>
{

}
