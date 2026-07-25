package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.util.UUID;

/**
 * Interface for Involved Party Type entities in the warehouse.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IInvolvedPartyType<J extends IInvolvedPartyType<J, Q>,
        Q extends IInvolvedPartyTypeQueryBuilder<Q, J>> extends IWarehouseBaseTable<J, Q, UUID>, IContainsNameAndDescription<J>
{

}