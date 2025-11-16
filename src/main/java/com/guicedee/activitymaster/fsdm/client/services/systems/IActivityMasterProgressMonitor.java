package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.guicedinjection.interfaces.IDefaultService;

@SuppressWarnings("UnusedReturnValue")
public interface IActivityMasterProgressMonitor extends IDefaultService<IActivityMasterProgressMonitor>
{
	/**
	 * Feeds progress information into the monitor
	 *
	 * @param source
	 * @param message
	 */
	IActivityMasterProgressMonitor progressUpdate(String source, String message);
	
	/**
	 * Returns the current task number that this progress monitor is on
	 * @return
	 */
	Integer getCurrentTask();
	
	/**
	 * Sets the current task value for this progress monitor
	 * @param i
	 * @return
	 */
	IActivityMasterProgressMonitor setCurrentTask(Integer i);
	
	/**
	 * Gets the total tasks
	 * @return
	 */
	Integer getTotalTasks();
	
	/**
	 * Sets the total number of tasks
	 * @param i
	 * @return
	 */
	IActivityMasterProgressMonitor setTotalTasks(Integer i);
}
