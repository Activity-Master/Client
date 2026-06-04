package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;

import java.util.UUID;


/**
 * Query builder for Involved Party Name Types.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the name type entity
 */
public interface IInvolvedPartyNameTypeQueryBuilder<J extends IInvolvedPartyNameTypeQueryBuilder<J, E>, E extends IInvolvedPartyNameType<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
