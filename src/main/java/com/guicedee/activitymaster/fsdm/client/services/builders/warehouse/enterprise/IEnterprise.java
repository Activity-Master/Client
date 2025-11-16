package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsNameAndDescription;

import java.io.Serializable;
import java.util.UUID;


//@Schema(name = "Enterprise")
public interface IEnterprise<J extends IEnterprise<J, Q>,
		Q extends IEnterpriseQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J,Q, UUID>,
		        IContainsNameAndDescription<J>,
		        IManageClassifications<J>
{

}
