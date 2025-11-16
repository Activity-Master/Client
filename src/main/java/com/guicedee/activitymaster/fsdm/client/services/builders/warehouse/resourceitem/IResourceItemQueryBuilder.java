package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.io.Serializable;
import java.util.UUID;


public interface IResourceItemQueryBuilder<J extends IResourceItemQueryBuilder<J,E>,E extends IResourceItem<E,J>>
		extends IQueryBuilderNamesAndDescriptions<J, E, UUID>,
		        IQueryBuilderClassifications<J, E, UUID>
{

}
