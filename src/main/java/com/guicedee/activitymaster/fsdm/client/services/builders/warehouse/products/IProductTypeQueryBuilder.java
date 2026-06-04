package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.util.UUID;


/**
 * Query builder for Product Types.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the product type entity
 */
public interface IProductTypeQueryBuilder<J extends IProductTypeQueryBuilder<J, E>, E extends IProductType<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderNamesAndDescriptions<J,E, UUID>
{

}
