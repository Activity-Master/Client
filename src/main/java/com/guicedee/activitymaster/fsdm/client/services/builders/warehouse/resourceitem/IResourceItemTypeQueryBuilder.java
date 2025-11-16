package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.io.Serializable;
import java.util.UUID;


public interface IResourceItemTypeQueryBuilder<J extends IResourceItemTypeQueryBuilder<J,E>,E extends IResourceItemType<E,J>>
		extends IQueryBuilderNamesAndDescriptions<J, E, UUID>
{

}
