package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

//import io.swagger.v3.oas.annotations.media.Schema;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.UUID;

//@Schema(description = "The UUID Representation of this interface",name = "InvolvedPartyType")
public interface IInvolvedPartyType<J extends IInvolvedPartyType<J, Q>,
		Q extends IInvolvedPartyTypeQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>
{

}