package com.guicedee.activitymaster.fsdm.client.services.systems;

/**
 * Reactivity Migration Checklist:
 * 
 * [✓] One action per Mutiny.Session at a time
 *     - All operations on a session are sequential
 *     - No parallel operations on the same session
 * 
 * [✓] Pass Mutiny.Session through the chain
 *     - All methods accept session as parameter
 *     - Session is passed to all dependent operations
 * 
 * [✓] No await() usage
 *     - Using reactive chains instead of blocking operations
 * 
 * [✓] Synchronous execution of reactive chains
 *     - All reactive chains execute synchronously
 *     - No fire-and-forget operations with subscribe().with()
 * 
 * [✓] No parallel operations on a session
 *     - Not using Uni.combine().all().unis() with operations that share the same session
 * 
 * [✓] No session/transaction creation in libraries
 *     - Sessions are passed in from the caller
 *     - No sessionFactory.withTransaction() in methods
 * 
 * See ReactivityMigrationGuide.md for more details on these rules.
 */

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Designates a system update, must be annotation with @Update for sorting and task information
 */
public interface ISystemUpdate extends IProgressable
{
	/**
	 * Perform an update
	 *
	 * @param session
	 * @param enterprise
	 */
	default Uni<Boolean> update(Mutiny.Session session, IEnterprise<?,?> enterprise)
    {
        return Uni.createFrom().failure(new UnsupportedOperationException(
                getClass().getSimpleName() + " has no session update overload"));
    }

	/**
	 * Stateless variant of {@link #update(Mutiny.Session, IEnterprise)}. Default throws to act as the
	 * incremental-migration seam: the install loop prefers this overload and falls back to the managed
	 * one when an updater has not been converted to run on a {@link Mutiny.StatelessSession}.
	 */
	default Uni<Boolean> update(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise)
	{
		return Uni.createFrom().failure(new UnsupportedOperationException(
				getClass().getSimpleName() + " has no stateless update overload"));
	}
}
