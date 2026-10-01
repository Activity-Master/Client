package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;

import java.util.UUID;


/**
 * Query builder for Address Types.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the address type entity
 */
public interface IAddressTypeQueryBuilder<J extends IAddressTypeQueryBuilder<J, E>, E extends IAddressType<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
