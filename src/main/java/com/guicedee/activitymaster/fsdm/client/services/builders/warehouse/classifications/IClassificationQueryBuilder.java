package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;

import java.io.Serializable;
import java.util.UUID;


/**
 * Query builder for Classifications.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the classification entity
 */
public interface IClassificationQueryBuilder<J extends IClassificationQueryBuilder<J, E>, E extends IClassification<E, J>>
		extends IQueryBuilderNamesAndDescriptions<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{
	/**
	 * Filters the query by a specific classification concept and system.
	 *
	 * @param concept       The classification data concept
	 * @param system        The system context
	 * @param identityToken Security tokens
	 * @return This builder
	 */
	IClassificationQueryBuilder<J,E> withConcept(EnterpriseClassificationDataConcepts concept, ISystems<?, ?> system, java.util.UUID... identityToken);
}
