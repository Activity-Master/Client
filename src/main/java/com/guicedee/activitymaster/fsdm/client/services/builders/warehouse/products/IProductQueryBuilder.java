package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;

import java.util.UUID;


/**
 * Query builder for Product entities.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the Product entity
 */
public interface IProductQueryBuilder<J extends IProductQueryBuilder<J, E>, E extends IProduct<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

}
