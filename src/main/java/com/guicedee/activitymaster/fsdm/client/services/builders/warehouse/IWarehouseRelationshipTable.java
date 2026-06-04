package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse;

import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Interface for warehouse tables that represent relationships between two other warehouse entities.
 *
 * @param <J>  The entity type
 * @param <Q>  The query builder type
 * @param <P>  The primary (parent) entity type
 * @param <S>  The secondary (child) entity type
 * @param <I>  The identifier type
 * @param <QS> The security table type
 */
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
