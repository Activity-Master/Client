package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-scoped accumulator that lets <strong>bulk loaders</strong> defer default-security creation
 * for just-created rows so it can be written in one batched, stateless transaction instead of paying
 * the per-row cost (~21 round-trips/row) inside {@link IWarehouseCoreTable#createDefaultSecurity(Mutiny.Session, ISystems, UUID...)}.
 *
 * <p>Usage contract:</p>
 * <ul>
 *   <li>A bulk loader {@link #activate(Mutiny.Session) activates} the collector for its session at the
 *       start of a load phase, and {@link #flush(Mutiny.Session, ISystems, UUID...) flushes} it at the end.</li>
 *   <li>While a session is active, capability mixins (e.g. {@code addClassification}) and loaders
 *       {@link #record(Mutiny.Session, IWarehouseCoreTable) record} the rows they create instead of
 *       calling per-row security. When a session is <em>not</em> active (ordinary single-entity
 *       creates), callers fall back to per-row {@code createDefaultSecurity} as before.</li>
 * </ul>
 *
 * <p>State is keyed by {@link Mutiny.Session}, so concurrent loads on different sessions never
 * interleave, and entries are removed on flush so nothing leaks across phases/enterprises. The state
 * is held statically (not via DI) to keep this usable from client-side capability mixins without any
 * binding/JPMS-opens requirements.</p>
 */
public final class DefaultSecurityCollector
{
	private static final Map<Mutiny.Session, List<IWarehouseCoreTable<?, ?, ?, ?>>> PENDING = new ConcurrentHashMap<>();
	private static final Set<Mutiny.Session> ACTIVE = ConcurrentHashMap.newKeySet();

	private DefaultSecurityCollector()
	{
	}

	/** Marks the session as a bulk-load context: capability mixins will batch security instead of per-row. */
	public static void activate(Mutiny.Session session)
	{
		if (session != null) ACTIVE.add(session);
	}

	/** @return {@code true} while the session is in a bulk-load context. */
	public static boolean isActive(Mutiny.Session session)
	{
		return session != null && ACTIVE.contains(session);
	}

	/** Records a just-created row to be secured at the next {@link #flush}. Synchronous, no round-trips. */
	public static void record(Mutiny.Session session, IWarehouseCoreTable<?, ?, ?, ?> row)
	{
		if (session == null || row == null) return;
		PENDING.computeIfAbsent(session, s -> new ArrayList<>()).add(row);
	}

	/**
	 * Secures every row recorded against {@code session} in one batched, stateless transaction, clears
	 * the session's pending set and deactivates it. A no-op when nothing was recorded.
	 */
	public static Uni<Void> flush(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken)
	{
		ACTIVE.remove(session);
		List<IWarehouseCoreTable<?, ?, ?, ?>> rows = PENDING.remove(session);
		if (rows == null || rows.isEmpty())
		{
			return Uni.createFrom().voidItem();
		}
		ISecurityTokenService<?> securityTokenService = IGuiceContext.get(ISecurityTokenService.class);
		return securityTokenService.applyDefaultSecurityToRows(session, rows, system, identityToken);
	}
}

