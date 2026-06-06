package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterProgressMonitor;
import lombok.extern.log4j.Log4j2;

/**
 * Default {@link IActivityMasterProgressMonitor} that logs progress to the application log.
 * <p>
 * To avoid flooding the log when a loader advances thousands of fine-grained tasks, milestone-less
 * count updates are only emitted when the completion percentage actually changes. Messages that
 * carry no counter movement (milestones) are always logged.
 */
@Log4j2
public class ConsoleLogActivityMasterProgressMaster implements IActivityMasterProgressMonitor
{
	private volatile int totalTasks = 0;
	private volatile int currentTasks = 0;
	private volatile int lastLoggedPercentage = -1;

	@Override
	public IActivityMasterProgressMonitor progressUpdate(String source, String message)
	{
		return progressUpdate(source, message, currentTasks, totalTasks);
	}

	@Override
	public IActivityMasterProgressMonitor progressUpdate(String source, String message, Integer currentTask, Integer totalTasks)
	{
		if (currentTask != null)
		{
			this.currentTasks = currentTask;
		}
		if (totalTasks != null)
		{
			this.totalTasks = totalTasks;
		}

		int percentage = getPercentageComplete();
		// Always log when the percentage moves (or when the total is unknown / work is just starting);
		// otherwise suppress the per-task chatter so the console stays readable.
		if (percentage != lastLoggedPercentage || this.totalTasks <= 0)
		{
			lastLoggedPercentage = percentage;
			log.info("[{}%] {} - {} ({}/{})", percentage, source, message, this.currentTasks, this.totalTasks);
		}
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
		currentTasks = i == null ? 0 : i;
		// Reset the percentage gate whenever the counter is rewound to the start of a new phase.
		if (currentTasks == 0)
		{
			lastLoggedPercentage = -1;
		}
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
		totalTasks = i == null ? 0 : i;
		return this;
	}
}
