package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.io.Serializable;
import java.util.UUID;


/**
 * Query builder for Active Flag entities.
 *
 * @param <Q> The type of the query builder
 * @param <E> The type of the Active Flag entity
 */
public interface IActiveFlagQueryBuilder<Q extends IActiveFlagQueryBuilder<Q, E>, E extends IActiveFlag<E, Q>>
		extends IQueryBuilderNamesAndDescriptions<Q, E, UUID>,
		        IQueryBuilderEnterprise<Q,E, UUID>
{

}
