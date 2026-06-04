package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.geography;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;

import java.util.UUID;


/**
 * Query builder for Geography entities.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the geography entity
 */
public interface IGeographyQueryBuilder<J extends IGeographyQueryBuilder<J,E>,E extends IGeography<E,J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
