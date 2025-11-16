package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.UUID;

public interface IWarehouseRelationshipClassificationTypeTable<
		J extends IWarehouseRelationshipClassificationTypeTable<J, Q, P, S, I,QS>,
		Q extends IQueryBuilderRelationships<Q, J, P, S, I>,
		P extends IWarehouseBaseTable<?, ?, ?>,
		S extends IWarehouseBaseTable<?, ?, ?>,
		I extends UUID,
		QS extends IWarehouseSecurityTable<QS,?,I>
		>
		extends IWarehouseRelationshipClassificationTable<J,Q,P,S,I,QS>
{

}
