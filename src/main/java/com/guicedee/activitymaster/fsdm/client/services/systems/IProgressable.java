package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;

import java.util.List;
import java.util.logging.Logger;

/**
 * Mix-in for any service that performs long-running, measurable work (system installs, system
 * updates, reference-data loads, etc.) and wishes to report its progress.
 * <p>
 * Progress is expressed as two counters - a <em>current</em> task count and a <em>total</em> task
 * count - plus a stream of human-readable messages. The actual state is held by the registered
 * {@link IActivityMasterProgressMonitor} SPI implementations (discovered via {@code ServiceLoader}),
 * which lets multiple consumers observe the same progress: a console logger, a WebSocket group
 * broadcaster, a metrics reporter, and so on.
 * <p>
 * Typical usage from a loader:
 * <pre>{@code
 * setCurrentTask(0);
 * setTotalTasks(records.size());          // tell observers how much work there is
 * for (Record r : records) {
 *     ... do the work ...
 *     logProgress("My Loader", "Loaded " + r.getName(), 1);   // advance by one and publish
 * }
 * logProgress("My Loader", "Finished loading");               // milestone message, no advance
 * }</pre>
 */
public interface IProgressable
{
	/**
	 * @return the registered progress monitors, never {@code null}
	 */
	private static List<IActivityMasterProgressMonitor> monitors()
	{
		return ActivityMasterConfiguration.get()
		                                  .getProgressMonitors();
	}

	/**
	 * @return the current task count, read from the first registered monitor, or {@code 0}
	 */
	default Integer getCurrentTask()
	{
		for (IActivityMasterProgressMonitor progressMonitor : monitors())
		{
			if (progressMonitor != null)
			{
				Integer current = progressMonitor.getCurrentTask();
				return current == null ? 0 : current;
			}
		}
		return 0;
	}

	/**
	 * @return the total task count, read from the first registered monitor, or {@code 0}
	 */
	default Integer getTotalTasks()
	{
		for (IActivityMasterProgressMonitor progressMonitor : monitors())
		{
			if (progressMonitor != null)
			{
				Integer total = progressMonitor.getTotalTasks();
				return total == null ? 0 : total;
			}
		}
		return 0;
	}

	/**
	 * Sets the current task count on every registered monitor.
	 *
	 * @param task the new current task value
	 */
	default void setCurrentTask(Integer task)
	{
		for (IActivityMasterProgressMonitor progressMonitor : monitors())
		{
			if (progressMonitor != null)
			{
				progressMonitor.setCurrentTask(task);
			}
		}
	}

	/**
	 * Sets the total task count on every registered monitor.
	 *
	 * @param task the new total task value
	 */
	default void setTotalTasks(Integer task)
	{
		for (IActivityMasterProgressMonitor progressMonitor : monitors())
		{
			if (progressMonitor != null)
			{
				progressMonitor.setTotalTasks(task);
			}
		}
	}

	/**
	 * Reports progress, optionally updating the total task count and advancing the current task
	 * count, then publishes the update (with both counters) to every registered monitor.
	 *
	 * @param source           the originating service/phase
	 * @param message          the message to publish
	 * @param currentTaskDelta the amount to advance the current task count by, or {@code null} to leave it unchanged
	 * @param totalTasks       the new total task count, or {@code null} to leave it unchanged
	 */
	default void logProgress(String source, String message, Integer currentTaskDelta, Integer totalTasks)
	{
		message = cleanName(message);

		// Compute the new counters once, from a single source of truth, so every monitor stays in sync.
		Integer newTotal = totalTasks != null ? totalTasks : getTotalTasks();
		Integer newCurrent = getCurrentTask() + (currentTaskDelta != null ? currentTaskDelta : 0);

		for (IActivityMasterProgressMonitor progressMonitor : monitors())
		{
			if (progressMonitor == null)
			{
				continue;
			}
			if (totalTasks != null)
			{
				progressMonitor.setTotalTasks(newTotal);
			}
			if (currentTaskDelta != null)
			{
				progressMonitor.setCurrentTask(newCurrent);
			}
			progressMonitor.progressUpdate(source, message, newCurrent, newTotal);
		}
	}

	/**
	 * Reports progress, advancing the current task count by the given delta and publishing the
	 * update to every registered monitor.
	 *
	 * @param source           the originating service/phase
	 * @param message          the message to publish
	 * @param currentTaskDelta the amount to advance the current task count by
	 */
	default void logProgress(String source, String message, Integer currentTaskDelta)
	{
		logProgress(source, message, currentTaskDelta, null);
	}

	/**
	 * Publishes a milestone progress message to every registered monitor <em>without</em> advancing
	 * the task counters. Use the {@code currentTaskDelta} overloads to actually advance progress so
	 * the totals stay accurate.
	 *
	 * @param source  the originating service/phase
	 * @param message the message to publish
	 */
	default void logProgress(String source, String message)
	{
		message = cleanName(message);
		Logger.getLogger(getClass().getCanonicalName())
		      .config(message);
		logProgress(source, message, null, null);
	}

	/**
	 * Just a prettifier for the names display
	 * @param name
	 * @return
	 */
	default String cleanName(String name)
	{
		if (name == null)
		{
			return "";
		}
		if (name.indexOf("$$EnhancerByGuice$$") > 0)
		{
			name = name.substring(0, name.indexOf("$$EnhancerByGuice$$"));
		}
		name = name.replaceAll("com\\.guicedee\\.activitymaster\\.activitymaster\\.systems\\.", "");
		name = name.replaceAll("com\\.guicedee\\.activitymaster\\.activitymaster\\.", "");
		return name;
	}
}
