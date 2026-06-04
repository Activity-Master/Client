package com.guicedee.activitymaster.fsdm.client.services;

import com.entityassist.enumerations.ActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Service interface for managing Active Flags within the system.
 * Active flags determine the visibility and state of records (e.g., Active, Archived, Deleted).
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IActiveFlagService<J extends IActiveFlagService<J>>
{
    /**
     * The name of the Active Flag system.
     */
    String ActivateFlagSystemName = "Active Flag System";

    /**
     * Finds an active flag by its name within a specific enterprise.
     *
     * @param session         The Mutiny session to use
     * @param flag            The name of the flag to find
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting the found active flag
     */
    Uni<IActiveFlag<?,?>> findFlagByName(Mutiny.Session session, String flag, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Gets a new, uninitialized active flag instance.
     *
     * @return A new active flag instance
     */
    IActiveFlag<?,?> get();

    /**
     * Finds an active flag by its enumeration value within a specific enterprise.
     *
     * @param session         The Mutiny session to use
     * @param flag            The ActiveFlag enum value
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting the found active flag
     */
    Uni<IActiveFlag<?,?>> findFlagByName(Mutiny.Session session, ActiveFlag flag, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Finds the range of active flags for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting a list of active flags in the active range
     */
    Uni<List<IActiveFlag<?,?>>> findActiveRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID ... identifyingToken);

    /**
     * Gets the range of visible active flags for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting a list of active flags in the visible range
     */
    Uni<List<IActiveFlag<?,?>>> getVisibleRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Gets the range of removed active flags for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting a list of active flags in the removed range
     */
    Uni<List<IActiveFlag<?,?>>> getRemovedRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Gets the range of archived active flags for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting a list of active flags in the archive range
     */
    Uni<List<IActiveFlag<?,?>>> getArchiveRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID ...identifyingToken);

    /**
     * Gets the range of highlighted active flags for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting a list of active flags in the highlighted range
     */
    Uni<List<IActiveFlag<?,?>>> getHighlightedRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Gets the default 'Active' flag for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting the 'Active' flag
     */
    Uni<IActiveFlag<?,?>> getActiveFlag(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID ...identifyingToken);

    /**
     * Gets the 'Archived' flag for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting the 'Archived' flag
     */
    Uni<IActiveFlag<?,?>> getArchivedFlag(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Gets the 'Deleted' flag for the given enterprise.
     *
     * @param session         The Mutiny session to use
     * @param enterprise      The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting the 'Deleted' flag
     */
    Uni<IActiveFlag<?,?>> getDeletedFlag(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Resolves an ActiveFlag ID (UUID) by its name within a specific enterprise using a lightweight
     * native SQL lookup with a small in-memory cache to reduce database load.
     */
    Uni<UUID> resolveActiveFlagIdByName(Mutiny.Session session, IEnterprise<?, ?> enterpriseId, String flagName);

    /**
     * Returns the set of ActiveFlag UUIDs for the VisibleRangeAndUp for the given enterprise.
     * Contract: never returns null. If a required flag is missing, lets NoResultException propagate.
     */
    default Uni<List<UUID>> getVisibleRangeAndUpIds(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
        return resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Archived.name())
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.LongTermStorage.name())
                .map(id -> { List<UUID> l = new ArrayList<>(); l.add(list); l.add(id); return l; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.MidTermStorage.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.ShortTermStorage.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Resolved.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Completed.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Active.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Current.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Important.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Highlighted.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Pending.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Always.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Permanent.name())
                .map(id -> { list.add(id); return list; }));
    }

    /**
     * Returns the set of ActiveFlag UUIDs for the RemovedRange for the given enterprise.
     * Contract: never returns null. If a required flag is missing, lets NoResultException propagate.
     */
    default Uni<List<UUID>> getRemovedRangeIds(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
        return resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Deleted.name())
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Hidden.name())
                .map(id -> { List<UUID> l = new ArrayList<>(); l.add(list); l.add(id); return l; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Invisible.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Errored.name())
                .map(id -> { list.add(id); return list; }));
    }
}
