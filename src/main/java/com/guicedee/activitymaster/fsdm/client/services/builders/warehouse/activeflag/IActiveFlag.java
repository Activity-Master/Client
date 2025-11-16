package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseNameAndDescriptionTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsEnterprise;

import java.io.Serializable;
import java.util.UUID;

//@Schema(name = "ActiveFlag")
public interface IActiveFlag<
		J extends IActiveFlag<J, Q>,
		Q extends IActiveFlagQueryBuilder<Q, J>
		>
		extends IWarehouseNameAndDescriptionTable<J, Q, UUID>,
		        IContainsEnterprise<J>,
		        IManageClassifications<J>
{
	
}
