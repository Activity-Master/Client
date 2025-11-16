package com.guicedee.activitymaster.fsdm.client.implementations;

import com.guicedee.guicedinjection.interfaces.IGuicePreStartup;

import java.util.List;

public class ActivityMasterClientPreStartup implements IGuicePreStartup<ActivityMasterClientPreStartup>
{
	@Override
	public List<io.vertx.core.Future<Boolean>> onStartup()
	{
		return List.of();
	}
}
