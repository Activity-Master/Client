package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;

import java.util.logging.Logger;

public interface IProgressable
{
	
	default Integer getCurrentTask()
	{
		for (IActivityMasterProgressMonitor progressMonitor : ActivityMasterConfiguration.get()
		                                                                                 .getProgressMonitors())
		{
			if (progressMonitor != null)
			{
				return progressMonitor.getCurrentTask();
			}
		}
		return 0;
	}
	
	default Integer getTotalTasks()
	{
		for (IActivityMasterProgressMonitor progressMonitor : ActivityMasterConfiguration.get()
		                                                                                 .getProgressMonitors())
		{
			if (progressMonitor != null)
			{
				return progressMonitor.getTotalTasks();
			}
		}
		return 0;
	}
	
	default void setCurrentTask(Integer task)
	{
		for (IActivityMasterProgressMonitor progressMonitor : ActivityMasterConfiguration.get()
		                                                                                 .getProgressMonitors())
		{
			if (progressMonitor != null)
			{
				progressMonitor.setCurrentTask(task);
			}
		}
	}
	
	default void setTotalTasks(Integer task)
	{
		for (IActivityMasterProgressMonitor progressMonitor : ActivityMasterConfiguration.get()
		                                                                                 .getProgressMonitors())
		{
			if (progressMonitor != null)
			{
				progressMonitor.setCurrentTask(task);
			}
		}
	}
	
	/**
	 * Interface for logging progress from a service to a client
	 * @param source The source of the message
	 * @param message The message
	 * @param currentTask The nymber of currentTask to increase by
	 * @param totalTasks The total task number
	 */
	default void logProgress(String source, String message, Integer currentTask, Integer totalTasks)
	{
		message = cleanName(message);
		for (IActivityMasterProgressMonitor progressMonitor : ActivityMasterConfiguration.get()
		                                                                                 .getProgressMonitors())
		{
			if (progressMonitor != null)
			{
				if (currentTask != null)
				{
					progressMonitor.setCurrentTask(getCurrentTask() + currentTask);
				}
				if (totalTasks != null)
				{
					progressMonitor.setTotalTasks(getTotalTasks() + totalTasks);
				}
				progressMonitor.progressUpdate(source, message);
			}
		}
	}
	
	/**
	 * Increments the log progress by the number of current tasks
	 * @param source
	 * @param message
	 * @param currentTask
	 */
	default void logProgress(String source, String message, Integer currentTask)
	{
		message = cleanName(message);
		for (IActivityMasterProgressMonitor progressMonitor : ActivityMasterConfiguration.get()
		                                                                                 .getProgressMonitors())
		{
			if (progressMonitor != null)
			{
				if (currentTask != null)
				{
					progressMonitor.setCurrentTask(getCurrentTask() + currentTask);
				}
			//	progressMonitor.progressUpdate(source, message);
			}
		}
		
	}
	
	/**
	 * Logs the progress with source and message set
	 *
	 * @param source
	 * @param message
	 */
	default void logProgress(String source, String message)
	{
		message = cleanName(message);
		Logger.getLogger(getClass().getCanonicalName()).config(message);
		for (IActivityMasterProgressMonitor progressMonitor : ActivityMasterConfiguration.get()
		                                                                                 .getProgressMonitors())
		{
			if (progressMonitor != null)
			{
				progressMonitor.progressUpdate(source, message);
				setCurrentTask(getCurrentTask() + 1);
			}
		}
	}
	
	/**
	 * Just a prettifier for the names display
	 * @param name
	 * @return
	 */
	default String cleanName(String name)
	{
		if (name.indexOf("$$EnhancerByGuice$$") > 0)
		{
			name = name.substring(0, name.indexOf("$$EnhancerByGuice$$"));
		}
		name = name.replaceAll("com\\.guicedee\\.activitymaster\\.activitymaster\\.systems\\.", "");
		name = name.replaceAll("com\\.guicedee\\.activitymaster\\.activitymaster\\.", "");
		return name;
	}
}
