package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maintenance helpers for Slowly Changing Dimension (SCD) relationship rows.
 *
 * <p>The "update + insert" flows (addOrUpdate / update) need to retire the current active row
 * (set its closing {@link IActiveFlag} and {@code effectiveToDate}) and then insert a brand new
 * version in the same transaction.</p>
 *
 * <p>Mutating the still-managed row and calling {@code session.merge(...)} is a no-op under
 * Hibernate Reactive with bytecode enhancement (self-dirty-tracking is not flushed), so the close
 * silently does not persist. Detaching the row before merging would fix the flush, but it corrupts
 * the subsequent {@code session.insert(newRow)} in the same flush cycle and throws
 * {@code AssertionFailure: possible non-threadsafe access to session}.</p>
 *
 * <p>The reliable approach for these combined flows is a bulk HQL {@code UPDATE} that closes the old
 * row by id. It executes as a standalone statement, bypasses the persistence context entirely, and
 * therefore never interferes with the following insert. Pure close operations (archive / remove /
 * expire — update only, no insert) keep using detach + merge.</p>
 */
public final class SCDLinkMaintenance
{
    private SCDLinkMaintenance()
    {
    }

    /**
     * Stateless variant of {@link #retireActiveRow(Mutiny.StatelessSession, Object, UUID, IActiveFlag, OffsetDateTime)}.
     * <p>
     * On a {@link Mutiny.StatelessSession} a bulk HQL {@code createMutationQuery(...)} is <strong>not</strong>
     * usable: it makes {@code org.hibernate.reactive} access {@code org.hibernate.query.hql.spi} in
     * {@code org.hibernate.orm.core}, which the ORM module does not export to the reactive module, throwing
     * {@code IllegalAccessError}. Since the caller already holds the loaded link row, close it with a full-row
     * {@code session.update}: it writes every column by id (no dirty tracking is required, so the lazy
     * {@code effectiveToDate} is persisted reliably). Returns {@code 1} to preserve the bulk-update contract.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Uni<Integer> retireActiveRow(Mutiny.StatelessSession session,
                                               Object managedRow,
                                               UUID rowId,
                                               IActiveFlag<?, ?> closingFlag,
                                               OffsetDateTime effectiveTo)
    {
        IWarehouseRelationshipTable row = (IWarehouseRelationshipTable) managedRow;
        row.setActiveFlagID(closingFlag);
        row.setEffectiveToDate(effectiveTo);
        return session.update(managedRow).replaceWith(1);
    }
}

