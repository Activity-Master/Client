package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;

import java.util.UUID;


public interface IResourceDataQueryBuilder<
		J extends IResourceDataQueryBuilder<J,E>,
		E extends IResourceData<E,J,?>
		>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
