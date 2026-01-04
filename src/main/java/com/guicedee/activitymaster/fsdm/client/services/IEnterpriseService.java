package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.IEnterpriseNames;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.systems.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public interface IEnterpriseService<J extends IEnterpriseService<J>> extends IProgressable
{
    String EnterpriseSystemName = "Enterprise System";

    IEnterprise<?, ?> get();

    Uni<Integer> loadUpdates(Mutiny.Session session, IEnterprise<?, ?> enterprise);

    Uni<Set<String>> getEnterpriseAppliedUpdates(Mutiny.Session session, IEnterprise<?, ?> enterprise);

    Uni<Map<Integer, Class<? extends ISystemUpdate>>> getUpdates(Mutiny.Session session, IEnterprise<?, ?> enterprise);

    Map<Integer, Class<? extends ISystemUpdate>> getAllUpdates();

    Uni<List<IEnterprise<?, ?>>> findEnterprisesWithClassification(Mutiny.Session session, IClassification<?, ?> classification);

    Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.Session session, UUID uuid);

    default Uni<Void> performPostStartup(Mutiny.Session session, IEnterprise<?, ?> enterprise)
    {
        ActivityMasterConfiguration configuration = ActivityMasterConfiguration.get();
        return configuration.isEnterpriseReady(session)
                       .chain(ent -> {
                           logProgress("System Loading", "Starting Systems... ", 1);
                           setCurrentTask(0);
                           Multi<IActivityMasterSystem<?>> multi = Multi.createFrom()
                                   .iterable(configuration.getAllSystems());
                           multi.invoke(iActivityMasterSystem -> {
                                       logProgress("System Loading", "Starting up system " + iActivityMasterSystem.getClass()
                                                                                                     .getName(), 1);
                                       // Call postStartup synchronously since it's not reactive yet
                                       iActivityMasterSystem.postStartup(session, enterprise).await().atMost(Duration.of(50L, ChronoUnit.SECONDS));
                                   })
                                   .onCompletion()
                                   .invoke(() -> {
                                       logProgress("System Loading", "Completed Startup of Systems... ", 1);
                                   })
                                   ;
                           multi.toUni()
                                   .await()
                                   .atMost(Duration.of(50L, ChronoUnit.SECONDS))
                           ;
                           return Uni.createFrom().voidItem();
                       })
                       .replaceWith(Uni.createFrom()
                                            .voidItem());
    }

    /**
     * Retrieves the enterprise by name only
     *
     * @param session
     * @param name
     * @return
     */
    Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.Session session, String name);
    default Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.Session session, IEnterpriseNames<?> name)
    {
        return getEnterprise(session, name.toString());
    }

    /**
     * Resolves an Enterprise ID (UUID) by its unique name using a lightweight native SQL lookup
     * with a small in-memory cache to reduce database load.
     */
    default Uni<UUID> resolveEnterpriseIdByName(Mutiny.Session session, String enterpriseName)
    {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getEnterpriseId(session, enterpriseName, (sess, name) -> {
                    String sql = "select enterpriseid from dbo.enterprise where enterprisename = :name";
                    return sess.createNativeQuery(sql)
                               .setParameter("name", name)
                               .getSingleResult()
                               .map(result -> (UUID) result);
                });
    }
				
    Uni<IEnterprise<?, ?>> startNewEnterprise(Mutiny.Session session, String enterpriseName,
                                              @NotNull String adminUserName, @NotNull String adminPassword);

    Uni<IEnterprise<?, ?>> startNewEnterprise(Mutiny.Session session, String enterpriseName,
                                              @NotNull String adminUserName, @NotNull String adminPassword, UUID uuidIdentifier);

    Uni<IEnterprise<?,?>> createNewEnterprise(Mutiny.Session session, @NotNull IEnterprise<?, ?> enterprise);

    Uni<IEnterprise<?, ?>> isEnterpriseReady(Mutiny.Session session);
}
