package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;


import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;

public interface IContainsSystem<J extends IContainsSystem<J>>
{
	ISystems<?,?> getSystemID();

	J setSystemID(ISystems<?,?> systemID);
}
