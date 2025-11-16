package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.io.Serializable;
import java.util.UUID;


public interface ISystemsQueryBuilder<J extends ISystemsQueryBuilder<J, E>, E extends ISystems<E, J>>
		extends IQueryBuilderNamesAndDescriptions<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
