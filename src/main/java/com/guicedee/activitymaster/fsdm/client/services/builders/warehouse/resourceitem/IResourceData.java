package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseSecurityTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsEnterprise;

import java.io.Serializable;
import java.util.UUID;

public interface IResourceData<J extends IResourceData<J, Q,S>,
		Q extends IResourceDataQueryBuilder<Q, J>,
		S extends IWarehouseSecurityTable<S,?, UUID>
		>
		extends IManageClassifications<J>,
		        IContainsEnterprise<J>,
						IWarehouseBaseTable<J, Q, UUID>
{
	
	byte[] getResourceItemData();

  J setResourceItemData(byte[] data);
}
