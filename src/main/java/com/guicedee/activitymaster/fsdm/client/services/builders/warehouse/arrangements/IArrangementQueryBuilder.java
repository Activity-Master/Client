package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;

import java.util.UUID;


public interface IArrangementQueryBuilder<J extends IArrangementQueryBuilder<J,E>,E extends IArrangement<E,J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>
{

}
