package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.client.services.IDefaultService;

@SuppressWarnings("UnusedReturnValue")
public interface IActivityMasterProgressMonitor extends IDefaultService<IActivityMasterProgressMonitor>
{
	/**
	 * Feeds progress information into the monitor.
	 * <p>
	 * This is the low-level callback. Implementations that need the task counters (for example a
	 * WebSocket group broadcaster reporting "x of y") should override
	 * {@link #progressUpdate(String, String, Integer, Integer)} instead, which carries the current
	 * and total task counts alongside the message.
	 *
	 * @param source  the originating service/phase (e.g. "Geography Service")
	 * @param message the human-readable progress message
	 */
	IActivityMasterProgressMonitor progressUpdate(String source, String message);

	/**
	 * Feeds progress information into the monitor together with the current task counters.
	 * <p>
	 * {@link IProgressable} always invokes this richer overload so SPI consumers receive the full
	 * picture (source, message, how far through the work we are and how many tasks there are in
	 * total). The default implementation simply delegates to {@link #progressUpdate(String, String)}
	 * for backwards compatibility; override it to publish the counts (for instance over a WebSocket
	 * group).
	 *
	 * @param source       the originating service/phase
	 * @param message      the human-readable progress message
	 * @param currentTask  the number of tasks completed so far (may be {@code null})
	 * @param totalTasks   the total number of tasks expected (may be {@code null} or {@code 0} when unknown)
	 * @return this monitor for chaining
	 */
	default IActivityMasterProgressMonitor progressUpdate(String source, String message, Integer currentTask, Integer totalTasks)
	{
		return progressUpdate(source, message);
	}

	/**
	 * Convenience helper computing the completion percentage (0-100) from the monitor's current and
	 * total task counters. Returns {@code 0} when the total is unknown.
	 *
	 * @return the completion percentage, clamped to the range {@code [0, 100]}
	 */
	default int getPercentageComplete()
	{
		Integer total = getTotalTasks();
		Integer current = getCurrentTask();
		if (total == null || total <= 0 || current == null || current <= 0)
		{
			return 0;
		}
		return Math.min(100, (int) Math.round((current * 100.0) / total));
	}

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
