package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterProgressMonitor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConsoleLogActivityMasterProgressMaster implements IActivityMasterProgressMonitor
{
	int totalTasks = 0;
	int currentTasks = 0;
	
	@Override
	public IActivityMasterProgressMonitor progressUpdate(String source, String message)
	{
		//log.info(source + " - " + message + " || " + currentTasks + "/" + totalTasks);
		return this;
	}
	
	@Override
	public Integer getCurrentTask()
	{
		return currentTasks;
	}
	
	@Override
	public IActivityMasterProgressMonitor setCurrentTask(Integer i)
	{
		currentTasks = i;
		return this;
	}
	
	@Override
	public Integer getTotalTasks()
	{
		return totalTasks;
	}
	
	@Override
	public IActivityMasterProgressMonitor setTotalTasks(Integer i)
	{
		totalTasks = i;
		return this;
	}
}
