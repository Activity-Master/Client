package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.geography;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;

import java.util.UUID;


public interface IGeographyQueryBuilder<J extends IGeographyQueryBuilder<J,E>,E extends IGeography<E,J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
