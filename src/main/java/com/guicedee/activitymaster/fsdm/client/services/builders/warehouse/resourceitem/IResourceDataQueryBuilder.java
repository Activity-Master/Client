package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;

import java.util.UUID;


/**
 * Query builder for Resource Data.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the resource data entity
 */
public interface IResourceDataQueryBuilder<
		J extends IResourceDataQueryBuilder<J,E>,
		E extends IResourceData<E,J,?>
		>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
