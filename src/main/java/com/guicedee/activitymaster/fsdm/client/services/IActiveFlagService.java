package com.guicedee.activitymaster.fsdm.client.services;

import com.entityassist.enumerations.ActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public interface IActiveFlagService<J extends IActiveFlagService<J>>
{
    String ActivateFlagSystemName = "Active Flag System";

    Uni<IActiveFlag<?,?>> findFlagByName(Mutiny.Session session, String flag, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    IActiveFlag<?,?> get();

    Uni<IActiveFlag<?,?>> findFlagByName(Mutiny.Session session, ActiveFlag flag, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    Uni<List<IActiveFlag<?,?>>> findActiveRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID ... identifyingToken);

    Uni<List<IActiveFlag<?,?>>> getVisibleRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    Uni<List<IActiveFlag<?,?>>> getRemovedRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    Uni<List<IActiveFlag<?,?>>> getArchiveRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID ...identifyingToken);

    Uni<List<IActiveFlag<?,?>>> getHighlightedRange(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    Uni<IActiveFlag<?,?>> getActiveFlag(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID ...identifyingToken);

    Uni<IActiveFlag<?,?>> getArchivedFlag(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    Uni<IActiveFlag<?,?>> getDeletedFlag(Mutiny.Session session, IEnterprise<?,?> enterprise, UUID... identifyingToken);

    /**
     * Resolves an ActiveFlag ID (UUID) by its name within a specific enterprise using a lightweight
     * native SQL lookup with a small in-memory cache to reduce database load.
     */
    default Uni<UUID> resolveActiveFlagIdByName(Mutiny.Session session, UUID enterpriseId, String flagName) {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getActiveFlagId(session, enterpriseId, flagName, (sess, name) -> {
                    String sql = "select activeflagid from dbo.activeflag where enterpriseid = :ent and activeflagname = :name";
                    return sess.createNativeQuery(sql)
                               .setParameter("ent", enterpriseId)
                               .setParameter("name", name)
                               .getSingleResult()
                               .map(result -> (UUID) result);
                });
    }

    /**
     * Returns the set of ActiveFlag UUIDs for the VisibleRangeAndUp for the given enterprise.
     * Contract: never returns null. If a required flag is missing, lets NoResultException propagate.
     */
    default Uni<List<UUID>> getVisibleRangeAndUpIds(Mutiny.Session session, UUID enterpriseId) {
        return resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Archived.name())
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.LongTermStorage.name())
                .map(id -> { List<UUID> l = new ArrayList<>(); l.add(list); l.add(id); return l; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.MidTermStorage.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.ShortTermStorage.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Resolved.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Completed.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Active.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Current.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Important.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Highlighted.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Pending.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Always.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Permanent.name())
                .map(id -> { list.add(id); return list; }));
    }

    /**
     * Returns the set of ActiveFlag UUIDs for the RemovedRange for the given enterprise.
     * Contract: never returns null. If a required flag is missing, lets NoResultException propagate.
     */
    default Uni<List<UUID>> getRemovedRangeIds(Mutiny.Session session, UUID enterpriseId) {
        return resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Deleted.name())
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Hidden.name())
                .map(id -> { List<UUID> l = new ArrayList<>(); l.add(list); l.add(id); return l; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Invisible.name())
                .map(id -> { list.add(id); return list; }))
            .flatMap(list -> resolveActiveFlagIdByName(session, enterpriseId, ActiveFlag.Errored.name())
                .map(id -> { list.add(id); return list; }));
    }
}
