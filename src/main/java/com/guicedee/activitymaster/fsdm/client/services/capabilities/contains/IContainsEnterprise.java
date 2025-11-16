package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;


import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;

public interface IContainsEnterprise<J extends IContainsEnterprise<J>>
{
	IEnterprise<?,?> getEnterpriseID();

	default IEnterprise<?,?> getEnterprise()
	{
		return getEnterpriseID();
	}

	J setEnterpriseID(IEnterprise<?,?> enterpriseID);
}
