package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseNameAndDescriptionTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;

import java.io.Serializable;
import java.util.UUID;

public interface IProduct<J extends IProduct<J,Q>, Q extends IProductQueryBuilder<Q,J>>
		extends IWarehouseNameAndDescriptionTable<J, Q, UUID>,
		        IManageClassifications<J>,
		        IManageResourceItems<J>,
		        IManageProductTypes<J>
{

}
