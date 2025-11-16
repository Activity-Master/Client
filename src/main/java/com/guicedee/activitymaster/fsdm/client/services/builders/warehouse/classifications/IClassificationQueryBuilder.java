package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;

import java.io.Serializable;
import java.util.UUID;


public interface IClassificationQueryBuilder<J extends IClassificationQueryBuilder<J, E>, E extends IClassification<E, J>>
		extends IQueryBuilderNamesAndDescriptions<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{
	IClassificationQueryBuilder<J,E> withConcept(EnterpriseClassificationDataConcepts concept, ISystems<?, ?> system, java.util.UUID... identityToken);
}
