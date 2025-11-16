package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseNameAndDescriptionTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageResourceItems;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;


public interface IClassification<J extends IClassification<J, Q>,
		Q extends IClassificationQueryBuilder<Q, J>>
		extends IWarehouseNameAndDescriptionTable<J, Q, UUID>,
		        IWarehouseBaseTable<J,Q, UUID>,
		        IContainsEnterprise<J>,
		        IContainsActiveFlags<J>,
		        IContainsSystem<J>,
		        IContainsHierarchy<J,java.util.UUID>,
		        IManageResourceItems<J>,
		        IManageClassifications<J>
{

}
