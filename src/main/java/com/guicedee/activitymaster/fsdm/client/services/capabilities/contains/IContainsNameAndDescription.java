package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;

public interface IContainsNameAndDescription<J extends IContainsNameAndDescription<J>>
{
	J setName(String name);

	J setDescription(String description);

	String getName();

	String getDescription();
}
