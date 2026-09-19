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
 * the per-row cost (~21 round-trips/row) inside {@link IWarehouseCoreTable#createDefaultSecurity(Mutiny.StatelessSession, ISystems, UUID...)}.
 *
 * <p>Usage contract:</p>
 * <ul>
 *   <li>A bulk loader {@link #activate(Mutiny.StatelessSession) activates} the collector for its session at the
 *       start of a load phase, and {@link #flush(Mutiny.StatelessSession, ISystems, UUID...) flushes} it at the end.</li>
 *   <li>While a session is active, capability mixins (e.g. {@code addClassification}) and loaders
 *       {@link #record(Mutiny.StatelessSession, IWarehouseCoreTable) record} the rows they create instead of
 *       calling per-row security. When a session is <em>not</em> active (ordinary single-entity
 *       creates), callers fall back to per-row {@code createDefaultSecurity} as before.</li>
 * </ul>
 *
 * <p>State is keyed by {@link Mutiny.StatelessSession}, so concurrent loads on different sessions never
 * interleave, and entries are removed on flush so nothing leaks across phases/enterprises. The state
 * is held statically (not via DI) to keep this usable from client-side capability mixins without any
 * binding/JPMS-opens requirements.</p>
 */
public final class DefaultSecurityCollector
{
	private static final Map<Mutiny.StatelessSession, List<IWarehouseCoreTable<?, ?, ?, ?>>> PENDING = new ConcurrentHashMap<>();
	private static final Set<Mutiny.StatelessSession> ACTIVE = ConcurrentHashMap.newKeySet();

	private static final Map<Mutiny.StatelessSession, List<IWarehouseCoreTable<?, ?, ?, ?>>> PENDING_SL = new ConcurrentHashMap<>();
	private static final Set<Mutiny.StatelessSession> ACTIVE_SL = ConcurrentHashMap.newKeySet();

	private DefaultSecurityCollector()
	{
	}

	// ---- Stateless twins ----

	public static void activate(Mutiny.StatelessSession session)
	{
		if (session != null) ACTIVE_SL.add(session);
	}

	public static boolean isActive(Mutiny.StatelessSession session)
	{
		return session != null && ACTIVE_SL.contains(session);
	}

	public static void record(Mutiny.StatelessSession session, IWarehouseCoreTable<?, ?, ?, ?> row)
	{
		if (session == null || row == null) return;
		PENDING_SL.computeIfAbsent(session, s -> new ArrayList<>()).add(row);
	}

	public static Uni<Void> flush(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken)
	{
		ACTIVE_SL.remove(session);
		List<IWarehouseCoreTable<?, ?, ?, ?>> rows = PENDING_SL.remove(session);
		if (rows == null || rows.isEmpty())
		{
			return Uni.createFrom().voidItem();
		}
		ISecurityTokenService<?> securityTokenService = IGuiceContext.get(ISecurityTokenService.class);
		return securityTokenService.applyDefaultSecurityToRows(session, rows, system, identityToken);
	}
}

