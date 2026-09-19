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

    /** Stateless variant of {@link #findFlagByName(Mutiny.StatelessSession, String, IEnterprise, UUID...)}. */
    Uni<IActiveFlag<?,?>> findFlagByName(Mutiny.StatelessSession session, String flag, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /** Stateless scope-restricted variant of {@link #createScopeRestricted(Mutiny.StatelessSession, IEnterprise, String, String, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, UUID...)}. */
    Uni<IActiveFlag<?,?>> createScopeRestricted(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, String name, String description,
                                                com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system,
                                                com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
                                                UUID... identifyingToken);

    /**
     * Gets a new, uninitialized active flag instance.
     *
     * @return A new active flag instance
     */
    IActiveFlag<?,?> get();

    /** Stateless variant of {@link #findFlagByName(Mutiny.StatelessSession, ActiveFlag, IEnterprise, UUID...)}. */
    Uni<IActiveFlag<?,?>> findFlagByName(Mutiny.StatelessSession session, ActiveFlag flag, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /** Stateless variant of {@link #findActiveRange(Mutiny.StatelessSession, IEnterprise, UUID...)}. */
    Uni<List<IActiveFlag<?,?>>> findActiveRange(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID ... identifyingToken);

    /** Stateless variant of {@link #getVisibleRange(Mutiny.StatelessSession, IEnterprise, UUID...)}. */
    Uni<List<IActiveFlag<?,?>>> getVisibleRange(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /** Stateless variant of {@link #getRemovedRange(Mutiny.StatelessSession, IEnterprise, UUID...)}. */
    Uni<List<IActiveFlag<?,?>>> getRemovedRange(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /** Stateless variant of {@link #getArchiveRange(Mutiny.StatelessSession, IEnterprise, UUID...)}. */
    Uni<List<IActiveFlag<?,?>>> getArchiveRange(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID ...identifyingToken);

    /** Stateless variant of {@link #getHighlightedRange(Mutiny.StatelessSession, IEnterprise, UUID...)}. */
    Uni<List<IActiveFlag<?,?>>> getHighlightedRange(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Stateless "fetch ids/scalars + prep" variant of {@link #getActiveFlag(Mutiny.StatelessSession, IEnterprise, UUID...)}.
     * <p>
     * {@code ActiveFlag} is {@code @Cacheable} but its {@code @ManyToOne enterpriseID} is {@code LAZY}, so the
     * only eager members are scalar columns. This projects the flag's own scalars
     * ({@code id, name, description, allowAccess}) and preps a fresh detached {@code ActiveFlag}, wiring the
     * enterprise reference from the supplied parameter. Returns the 'Active' flag — exactly the FK reference the
     * stateless default-security insert API needs.
     *
     * @param session          The stateless session to use
     * @param enterprise       The enterprise to search within
     * @param identifyingToken Optional security identity tokens
     * @return A Uni emitting the prepped, detached 'Active' flag
     */
    Uni<IActiveFlag<?,?>> getActiveFlag(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID ...identifyingToken);


    /** Stateless "fetch ids/scalars + prep" variant of {@link #getArchivedFlag(Mutiny.StatelessSession, IEnterprise, UUID...)}. */
    Uni<IActiveFlag<?,?>> getArchivedFlag(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /** Stateless "fetch ids/scalars + prep" variant of {@link #getDeletedFlag(Mutiny.StatelessSession, IEnterprise, UUID...)}. */
    Uni<IActiveFlag<?,?>> getDeletedFlag(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /** Stateless variant of {@link #resolveActiveFlagIdByName(Mutiny.StatelessSession, IEnterprise, String)}. */
    Uni<UUID> resolveActiveFlagIdByName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterpriseId, String flagName);


    /** Stateless variant of {@link #getVisibleRangeAndUpIds(Mutiny.StatelessSession, IEnterprise)}. */
    default Uni<List<UUID>> getVisibleRangeAndUpIds(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
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

    /** Stateless variant of {@link #getRemovedRangeIds(Mutiny.StatelessSession, IEnterprise)}. */
    default Uni<List<UUID>> getRemovedRangeIds(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        return resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Deleted.name())
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Hidden.name())
                .map(id -> { List<UUID> l = new ArrayList<>(); l.add(list); l.add(id); return l; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Invisible.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterprise, ActiveFlag.Errored.name())
                .map(id -> { list.add(id); return list; }));
    }
}
