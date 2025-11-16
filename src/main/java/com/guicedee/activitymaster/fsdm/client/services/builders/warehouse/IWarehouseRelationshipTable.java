package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse;

import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;

public interface IWarehouseRelationshipTable<
		J extends IWarehouseRelationshipTable<J, Q, P, S, I,QS>,
		Q extends IQueryBuilderRelationships<Q, J, P, S, I>,
		P extends IWarehouseBaseTable<?, ?, ?>,
		S extends IWarehouseBaseTable<?, ?, ?>,
		I extends UUID,
		QS extends IWarehouseSecurityTable<QS,?,I>
		>
		extends IWarehouseTable<J, Q, I,QS>,
		        IRelationshipValue<P, S,J>,
		        IContainsActiveFlags<J>,
		        IContainsEnterprise<J>,
		        IContainsSystem<J>
{

}
