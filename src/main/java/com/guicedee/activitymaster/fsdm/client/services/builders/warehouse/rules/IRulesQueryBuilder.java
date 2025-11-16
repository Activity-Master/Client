package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules;

import com.guicedee.activitymaster.fsdm.client.services.builders.*;

import java.util.UUID;


public interface IRulesQueryBuilder<J extends IRulesQueryBuilder<J, E>, E extends IRules<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderNamesAndDescriptions<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
