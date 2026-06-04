package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;

import java.util.UUID;


/**
 * Query builder for Event Types.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the event type entity
 */
public interface IEventTypeQueryBuilder<J extends IEventTypeQueryBuilder<J, E>, E extends IEventType<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

	
}
