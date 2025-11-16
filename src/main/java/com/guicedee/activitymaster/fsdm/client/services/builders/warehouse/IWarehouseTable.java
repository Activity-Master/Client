package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsRowRecordInformation;

import java.io.Serializable;
import java.util.UUID;

public interface IWarehouseTable<
		J extends IWarehouseTable<J, Q, I, S>,
		Q extends IQueryBuilderDefault<Q, J, I>,
		I extends UUID,
		S extends IWarehouseSecurityTable<S, ?, I>
		>
		extends IWarehouseCoreTable<J, Q, I, S>, Serializable,
		        IContainsRowRecordInformation<J>
{

}
