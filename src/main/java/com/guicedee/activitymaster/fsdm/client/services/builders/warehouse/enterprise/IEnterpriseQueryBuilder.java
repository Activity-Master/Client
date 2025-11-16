package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.util.UUID;


public interface IEnterpriseQueryBuilder<
		J extends IEnterpriseQueryBuilder<J, E>,
		E extends IEnterprise<E, J>
		>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderNamesAndDescriptions<J, E, UUID>
{
	default J withEnterprise(IEnterprise<?, ?> enterprise)
	{
		return withName(enterprise.getName());
	}
	default J withEnterprise(String enterprise)
	{
		return withName(enterprise);
	}

}
