package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseNameAndDescriptionTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageResourceItems;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;

public interface IClassificationDataConcept<J extends IClassificationDataConcept<J, Q>,
		Q extends IClassificationDataConceptQueryBuilder<Q, J>>
		extends IWarehouseNameAndDescriptionTable<J, Q, UUID>,
		        IContainsEnterprise<J>,
		        IContainsActiveFlags<J>,
		        IContainsSystem<J>,
		        IManageResourceItems<J>
{
}
