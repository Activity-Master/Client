package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address;

import com.guicedee.activitymaster.fsdm.client.services.builders.*;

import java.util.UUID;


/**
 * Query builder for Address entities.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the Address entity
 */
public interface IAddressQueryBuilder<J extends IAddressQueryBuilder<J,E>,E extends IAddress<E,J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderValues<J,E, UUID>
{

}
