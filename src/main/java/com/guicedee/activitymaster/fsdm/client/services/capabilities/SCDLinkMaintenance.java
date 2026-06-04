package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
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
 * the subsequent {@code session.persist(newRow)} in the same flush cycle and throws
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
     * Closes (retires) a single active SCD relationship row via a bulk UPDATE, by id.
     *
     * @param session     the active Mutiny session
     * @param managedRow  the row being retired (used only to resolve the entity name)
     * @param rowId       the identifier of the row to close
     * @param closingFlag the flag to apply (archived / deleted)
     * @param effectiveTo the effective-to timestamp marking the close
     * @return the number of rows updated (expected 1)
     */
    public static Uni<Integer> retireActiveRow(Mutiny.Session session,
                                               Object managedRow,
                                               UUID rowId,
                                               IActiveFlag<?, ?> closingFlag,
                                               OffsetDateTime effectiveTo)
    {
        String entityName = org.hibernate.Hibernate.getClass(managedRow)
                                                    .getSimpleName();
        return session.createMutationQuery(
                              "update " + entityName + " e set e.activeFlagID = :flag, e.effectiveToDate = :eff where e.id = :id")
                      .setParameter("flag", closingFlag)
                      .setParameter("eff", effectiveTo)
                      .setParameter("id", rowId)
                      .executeUpdate();
    }
}

