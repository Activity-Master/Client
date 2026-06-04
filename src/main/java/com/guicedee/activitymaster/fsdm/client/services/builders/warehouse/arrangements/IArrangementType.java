package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Interface for Arrangement Type entities in the warehouse.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IArrangementType<J extends IArrangementType<J, Q>,
		Q extends IArrangementTypesQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J,Q, UUID>,
		        IContainsNameAndDescription<J>,
		        IContainsSystem<J>,
		        IContainsEnterprise<J>
	
{
}
