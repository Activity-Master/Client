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

/**
 * Service interface for managing enterprises.
 * Provides methods for loading updates, retrieving enterprises, and managing enterprise lifecycle.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IEnterpriseService<J extends IEnterpriseService<J>> extends IProgressable
{
    /**
     * The name of the Enterprise system.
     */
    String EnterpriseSystemName = "Enterprise System";

    /**
     * Gets a new, uninitialized enterprise instance.
     *
     * @return A new enterprise instance
     */
    IEnterprise<?, ?> get();

    /**
     * Loads system updates for the specified enterprise.
     *
     * @param session    The Mutiny session to use
     * @param enterprise The enterprise to load updates for
     * @return A Uni emitting the number of updates loaded
     */
    Uni<Integer> loadUpdates(Mutiny.Session session, IEnterprise<?, ?> enterprise);

    /**
     * Retrieves the set of applied update names for an enterprise.
     *
     * @param session    The Mutiny session to use
     * @param enterprise The enterprise to check
     * @return A Uni emitting a set of applied update names
     */
    Uni<Set<String>> getEnterpriseAppliedUpdates(Mutiny.Session session, IEnterprise<?, ?> enterprise);

    /**
     * Retrieves the available updates for an enterprise.
     *
     * @param session    The Mutiny session to use
     * @param enterprise The enterprise to check
     * @return A Uni emitting a map of update versions to their classes
     */
    Uni<Map<Integer, Class<? extends ISystemUpdate>>> getUpdates(Mutiny.Session session, IEnterprise<?, ?> enterprise);

    /**
     * Gets all possible updates for the system.
     *
     * @return A map of all available updates
     */
    Map<Integer, Class<? extends ISystemUpdate>> getAllUpdates();

    /**
     * Finds enterprises that have a specific classification.
     *
     * @param session        The Mutiny session to use
     * @param classification The classification to search for
     * @return A Uni emitting a list of matching enterprises
     */
    Uni<List<IEnterprise<?, ?>>> findEnterprisesWithClassification(Mutiny.Session session, IClassification<?, ?> classification);

    /**
     * Retrieves an enterprise by name using a stateless session.
     *
     * @param session The stateless session to use
     * @param name    The name of the enterprise
     * @return A Uni emitting the found enterprise
     */
    Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.StatelessSession session, String name);

    /**
     * Retrieves an enterprise by its unique ID.
     *
     * @param session The Mutiny session to use
     * @param uuid    The UUID of the enterprise
     * @return A Uni emitting the found enterprise
     */
    Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.Session session, UUID uuid);

    /**
     * Performs post-startup operations for an enterprise.
     *
     * @param session    The Mutiny session to use
     * @param enterprise The enterprise to initialize
     * @return A Uni that completes when the operation is finished
     */
    default Uni<Void> performPostStartup(Mutiny.Session session, IEnterprise<?, ?> enterprise)
    {
        ActivityMasterConfiguration configuration = ActivityMasterConfiguration.get();
        return configuration.isEnterpriseReady(session)
                       .chain(ent -> {
                           logProgress("System Loading", "Starting Systems... ", 1);
                           setCurrentTask(0);
                           Multi<IMasterSystem<?>> multi = Multi.createFrom()
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
     * Retrieves the enterprise by name only.
     *
     * @param session The Mutiny session to use
     * @param name    The name of the enterprise
     * @return A Uni emitting the found enterprise
     */
    Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.Session session, String name);

    /**
     * Retrieves an enterprise using an enterprise names object.
     *
     * @param session The Mutiny session to use
     * @param name    The enterprise name object
     * @return A Uni emitting the found enterprise
     */
    default Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.Session session, IEnterpriseNames<?> name)
    {
        return getEnterprise(session, name.toString());
    }

    /**
     * Resolves an Enterprise ID (UUID) by its unique name using a lightweight native SQL lookup
     * with a small in-memory cache to reduce database load.
     *
     * @param session        The Mutiny session to use
     * @param enterpriseName The name of the enterprise
     * @return A Uni emitting the UUID of the enterprise
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

    /**
     * Starts a new enterprise with an administrator user.
     *
     * @param session        The Mutiny session to use
     * @param enterpriseName The name of the new enterprise
     * @param adminUserName  The administrator username
     * @param adminPassword  The administrator password
     * @return A Uni emitting the created enterprise
     */
    Uni<IEnterprise<?, ?>> startNewEnterprise(Mutiny.Session session, String enterpriseName,
                                              @NotNull String adminUserName, @NotNull String adminPassword);

    /**
     * Starts a new enterprise with a specific ID.
     *
     * @param session        The Mutiny session to use
     * @param enterpriseName The name of the new enterprise
     * @param adminUserName  The administrator username
     * @param adminPassword  The administrator password
     * @param uuidIdentifier The specific UUID to use for the enterprise
     * @return A Uni emitting the created enterprise
     */
    Uni<IEnterprise<?, ?>> startNewEnterprise(Mutiny.Session session, String enterpriseName,
                                              @NotNull String adminUserName, @NotNull String adminPassword, UUID uuidIdentifier);

    /**
     * Creates a new enterprise from an existing enterprise object.
     *
     * @param session    The Mutiny session to use
     * @param enterprise The enterprise object to create
     * @return A Uni emitting the created enterprise
     */
    Uni<IEnterprise<?,?>> createNewEnterprise(Mutiny.Session session, @NotNull IEnterprise<?, ?> enterprise);

    /**
     * Checks if the enterprise is ready.
     *
     * @param session The Mutiny session to use
     * @return A Uni emitting the enterprise if ready
     */
    Uni<IEnterprise<?, ?>> isEnterpriseReady(Mutiny.Session session);
}
