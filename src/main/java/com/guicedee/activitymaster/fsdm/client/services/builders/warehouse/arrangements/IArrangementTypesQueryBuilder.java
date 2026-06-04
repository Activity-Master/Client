package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements;

import com.guicedee.activitymaster.fsdm.client.services.builders.*;

import java.util.UUID;


/**
 * Query builder for Arrangement Types.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the arrangement type entity
 */
public interface IArrangementTypesQueryBuilder<J extends IArrangementTypesQueryBuilder<J,E>,E extends IArrangementType<E,J>>
		extends IQueryBuilderSCD<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderNamesAndDescriptions<J,E, UUID>
{

}
