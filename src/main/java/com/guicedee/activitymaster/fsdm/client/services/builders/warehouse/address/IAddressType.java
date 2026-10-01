package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.util.UUID;

/**
 * Interface for Address Type entities in the warehouse.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IAddressType<J extends IAddressType<J, Q>,
        Q extends IAddressTypeQueryBuilder<Q, J>> extends IWarehouseBaseTable<J, Q, UUID>, IContainsNameAndDescription<J>
{

}