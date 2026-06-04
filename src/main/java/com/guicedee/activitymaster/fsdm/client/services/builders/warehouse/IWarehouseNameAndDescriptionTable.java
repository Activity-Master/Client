package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.io.Serializable;
import java.util.UUID;

/**
 * Interface for warehouse tables that include Name and Description fields.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 * @param <I> The identifier type
 */
public interface IWarehouseNameAndDescriptionTable<J extends IWarehouseNameAndDescriptionTable<J, Q, I>,
		Q extends IQueryBuilderClassifications<Q, J, I>,
		I extends UUID>
		extends IWarehouseBaseTable<J, Q, I>,
		        IContainsNameAndDescription<J>
{

}
