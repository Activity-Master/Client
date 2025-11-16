package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

//import io.swagger.v3.oas.annotations.media.Schema;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.UUID;


//@Schema(type = "string", format = "uuid",description = "The UUID Representation of this interface",name = "InvolvedPartyIdentificationType")
public interface IInvolvedPartyIdentificationType<J extends IInvolvedPartyIdentificationType<J, Q>,
		Q extends IInvolvedPartyIdentificationTypeQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>
{

}
