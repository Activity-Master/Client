package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

//import io.swagger.v3.oas.annotations.media.Schema;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.UUID;

//@Schema(description = "The UUID Representation of this interface",name = "InvolvedPartyType")
/**
 * Interface for Involved Party Type entities in the warehouse.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IInvolvedPartyType<J extends IInvolvedPartyType<J, Q>,
		Q extends IInvolvedPartyTypeQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>
{

}