package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

//import io.swagger.v3.oas.annotations.media.Schema;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.UUID;

//@Schema(description = "The UUID Representation of this interface",name = "InvolvedPartyNameType")
public interface IInvolvedPartyNameType<J extends IInvolvedPartyNameType<J, Q>,
		Q extends IInvolvedPartyNameTypeQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>
{

}