package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications;

import com.guicedee.activitymaster.fsdm.client.services.builders.*;

import java.io.Serializable;
import java.util.UUID;


/**
 * Query builder for Classification Data Concepts.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the data concept entity
 */
public interface IClassificationDataConceptQueryBuilder<J extends IClassificationDataConceptQueryBuilder<J, E>, E extends IClassificationDataConcept<E, J>>
		extends IQueryBuilderFlags<J, E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderNamesAndDescriptions<J,E, UUID>
{

}
