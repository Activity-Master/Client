package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

/**
 * Default query builder interface for Activity Master entities.
 * Extends SCD capabilities for standard warehouse tables.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier (usually UUID)
 */
public interface IQueryBuilderDefault<J extends IQueryBuilderDefault<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>,
		I extends UUID>
		extends IQueryBuilderSCD<J, E, I>
{
	/**
	 * Synchronous query-level <strong>read</strong> trim, available on every default entity query.
	 * Restricts the result set to the supplied pre-resolved readable entity ids — the ids returned by
	 * {@link com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable#readableIds(org.hibernate.reactive.mutiny.Mutiny.Session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems, UUID...)},
	 * which already encodes the {@code securityTokenID IN (applicable)} &and; {@code ReadAllowed = true}
	 * (and in-date-range) rule against this entity's security table.
	 * <p>
	 * Because the applicable-token expansion is resolved with a reactive {@code WITH RECURSIVE} query, it
	 * cannot run inside this synchronous fluent method; resolve the ids first with {@code readableIds(...)}
	 * and then apply them here before {@code getAll()} so list queries are automatically security-trimmed.
	 * <p>
	 * Behaviour:
	 * <ul>
	 *     <li>security disabled in {@link ActivityMasterConfiguration} &rarr; pass-through (no filtering);</li>
	 *     <li>{@code null} ids (unresolved) &rarr; pass-through;</li>
	 *     <li>empty ids (resolved, but nothing readable) &rarr; deny-all (filters to a sentinel id that
	 *         cannot match any real row, so the query returns no rows).</li>
	 * </ul>
	 *
	 * @param readableEntityIds the pre-resolved ids of this entity type that the caller may read
	 * @return This builder
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	default J canRead(Collection<UUID> readableEntityIds) {
		if (!ActivityMasterConfiguration.get().isSecurityEnabled()) {
			return (J) this;
		}
		if (readableEntityIds == null) {
			return (J) this;
		}
		if (readableEntityIds.isEmpty()) {
			// Deny-all: an all-zero sentinel id that cannot collide with a real (random) UUID.
			where("id", com.entityassist.enumerations.Operand.InList,
					java.util.List.of(new UUID(0L, 0L)));
			return (J) this;
		}
		where("id", com.entityassist.enumerations.Operand.InList, readableEntityIds);
		return (J) this;
	}
}
