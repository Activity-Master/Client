package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.io.Serializable;
import java.util.UUID;


/**
 * Query builder for Resource Item Types.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the resource item type entity
 */
public interface IResourceItemTypeQueryBuilder<J extends IResourceItemTypeQueryBuilder<J,E>,E extends IResourceItemType<E,J>>
		extends IQueryBuilderNamesAndDescriptions<J, E, UUID>
{

}
