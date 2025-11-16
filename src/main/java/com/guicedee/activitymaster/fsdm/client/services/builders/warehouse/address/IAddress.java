package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;


public interface IAddress<J extends IAddress<J, Q>,
		Q extends IAddressQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IManageClassifications<J>,
		        IManageResourceItems<J>,
		        IContainsActiveFlags<J>,
		        IContainsEnterprise<J>,
		        IContainsSystem<J>,
		        IContainsClassifications<J>,
		        IManageGeographies<J>
{
	String getValue();
	
	J setValue(String value);
}
