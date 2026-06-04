package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.util.UUID;


/**
 * Query builder for Enterprise entities.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the enterprise entity
 */
public interface IEnterpriseQueryBuilder<
		J extends IEnterpriseQueryBuilder<J, E>,
		E extends IEnterprise<E, J>
		>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderNamesAndDescriptions<J, E, UUID>
{
	/**
	 * Filters by the name of the specified enterprise.
	 *
	 * @param enterprise The enterprise to filter by
	 * @return This builder
	 */
	default J withEnterprise(IEnterprise<?, ?> enterprise)
	{
		return withName(enterprise.getName());
	}

	/**
	 * Filters by enterprise name.
	 *
	 * @param enterprise The enterprise name
	 * @return This builder
	 */
	default J withEnterprise(String enterprise)
	{
		return withName(enterprise);
	}

}
