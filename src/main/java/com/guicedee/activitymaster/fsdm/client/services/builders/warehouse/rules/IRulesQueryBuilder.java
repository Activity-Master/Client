package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules;

import com.guicedee.activitymaster.fsdm.client.services.builders.*;

import java.util.UUID;


/**
 * Query builder for Rules.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the rules entity
 */
public interface IRulesQueryBuilder<J extends IRulesQueryBuilder<J, E>, E extends IRules<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderNamesAndDescriptions<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
