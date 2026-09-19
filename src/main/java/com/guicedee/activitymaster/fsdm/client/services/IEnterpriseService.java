package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.IEnterpriseNames;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache;
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
public interface IEnterpriseService<J extends IEnterpriseService<J>> extends IProgressable {
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
     * Find-or-create the lean enterprise record on a {@link Mutiny.StatelessSession}.
     * <p>
     * A stateless session has no persistence context, so the insert is a pure JDBC write (batchable,
     * no dirty-checking). Idempotent: an existing in-date-range enterprise with the same name is
     * returned instead of inserting a duplicate.
     *
     * @param session     The stateless session to use
     * @param name        The enterprise name
     * @param description The enterprise description
     * @return A Uni emitting the found or newly-created enterprise
     */
    Uni<IEnterprise<?, ?>> create(Mutiny.StatelessSession session, String name, String description);

    /**
     * Stateless variant of {@link #loadUpdates(Mutiny.StatelessSession, IEnterprise)}.
     */
    Uni<Integer> loadUpdates(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise);

    /**
     * Stateless variant of {@link #getEnterpriseAppliedUpdates(Mutiny.StatelessSession, IEnterprise)}.
     */
    Uni<Set<String>> getEnterpriseAppliedUpdates(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise);

    /**
     * Stateless variant of {@link #getUpdates(Mutiny.StatelessSession, IEnterprise)}.
     */
    Uni<Map<Integer, Class<? extends ISystemUpdate>>> getUpdates(Mutiny.StatelessSession session,
                                                                 IEnterprise<?, ?> enterprise);

    /**
     * Gets all possible updates for the system.
     *
     * @return A map of all available updates
     */
    Map<Integer, Class<? extends ISystemUpdate>> getAllUpdates();

    /**
     * Stateless variant of {@link #findEnterprisesWithClassification(Mutiny.StatelessSession, IClassification)}.
     */
    Uni<List<IEnterprise<?, ?>>> findEnterprisesWithClassification(Mutiny.StatelessSession session,
                                                                   IClassification<?, ?> classification);

    /**
     * Retrieves an enterprise by name using a stateless session.
     *
     * @param session The stateless session to use
     * @param name    The name of the enterprise
     * @return A Uni emitting the found enterprise
     */
    Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.StatelessSession session, String name);

    /**
     * Retrieves an enterprise by its unique ID using a stateless session.
     *
     * @param session The stateless session to use
     * @param uuid    The UUID of the enterprise
     * @return A Uni emitting the found enterprise
     */
    Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.StatelessSession session, UUID uuid);

    /**
     * Stateless variant of {@link #performPostStartup(Mutiny.StatelessSession, IEnterprise)} — runs each registered
     * system's {@link IMasterSystem#postStartup(Mutiny.StatelessSession, IEnterprise)} sequentially on the
     * supplied {@link Mutiny.StatelessSession} (no blocking {@code await}, one operation at a time).
     */
    default Uni<Void> performPostStartup(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        ActivityMasterConfiguration configuration = ActivityMasterConfiguration.get();
        java.util.List<IMasterSystem<?>> systems = new java.util.ArrayList<>(configuration.getAllSystems());
        logProgress("System Loading", "Starting Systems... ", 1);
        setCurrentTask(0);
        Uni<Void> chain = Uni.createFrom().voidItem();
        for (IMasterSystem<?> system : systems) {
            final IMasterSystem<?> current = system;
            chain = chain.chain(() -> {
                logProgress("System Loading", "Starting up system " + current.getClass().getName(), 1);
                return current.postStartup(session, enterprise);
            });
        }
        return chain.invoke(() -> logProgress("System Loading", "Completed Startup of Systems... ", 1));
    }

    /**
     * Retrieves an enterprise using an enterprise names object.
     *
     * @param session The Mutiny session to use
     * @param name    The enterprise name object
     * @return A Uni emitting the found enterprise
     */
    default Uni<IEnterprise<?, ?>> getEnterprise(Mutiny.StatelessSession session, IEnterpriseNames<?> name) {
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
    default Uni<UUID> resolveEnterpriseIdByName(Mutiny.StatelessSession session, String enterpriseName) {
        return NameIdCache.getEnterpriseId(session, enterpriseName, (sess, name) -> {
            String sql = "select enterpriseid from dbo.enterprise where enterprisename = :name";
            return sess.createNativeQuery(sql).setParameter("name", name).getSingleResult()
                       .map(result -> (UUID) result);
        });
    }

    /**
     * Starts a new enterprise driven from a stateless session.
     * <p>
     * The lean enterprise record is seeded through the supplied {@link Mutiny.StatelessSession}
     * (no persistence context, JDBC-batchable insert); the deeper system install — which needs a
     * managed persistence context — is orchestrated on internally-managed stateful sessions.
     * <p>
     * <strong>Top-level entry point:</strong> this method opens and manages its own sessions, so do
     * <em>not</em> invoke it from within another already-open transaction.
     *
     * @param session        The stateless session to use to seed the enterprise record
     * @param enterpriseName The name of the new enterprise
     * @param adminUserName  The administrator username
     * @param adminPassword  The administrator password
     * @return A Uni emitting the created enterprise
     */
    Uni<IEnterprise<?, ?>> startNewEnterprise(Mutiny.StatelessSession session,
                                              String enterpriseName,
                                              @NotNull String adminUserName,
                                              @NotNull String adminPassword);

    /**
     * Starts a new enterprise with a specific ID, driven from a stateless session.
     *
     * @param session        The stateless session to use to seed the enterprise record
     * @param enterpriseName The name of the new enterprise
     * @param adminUserName  The administrator username
     * @param adminPassword  The administrator password
     * @param uuidIdentifier The specific UUID to use for the enterprise
     * @return A Uni emitting the created enterprise
     * @see #startNewEnterprise(Mutiny.StatelessSession, String, String, String)
     */
    Uni<IEnterprise<?, ?>> startNewEnterprise(Mutiny.StatelessSession session,
                                              String enterpriseName,
                                              @NotNull String adminUserName,
                                              @NotNull String adminPassword,
                                              UUID uuidIdentifier);

    /**
     * Creates a new enterprise from an existing enterprise object, driven from a stateless session.
     * <p>
     * Enterprise creation self-manages its own sessions/transactions internally, so the lifecycle is
     * identical regardless of the caller's session kind. Call as a top-level entry point.
     *
     * @param session    The stateless session to use
     * @param enterprise The enterprise object to create
     * @return A Uni emitting the created enterprise
     */
    Uni<IEnterprise<?, ?>> createNewEnterprise(Mutiny.StatelessSession session, @NotNull IEnterprise<?, ?> enterprise);

    /**
     * Stateless variant of {@link #isEnterpriseReady(Mutiny.StatelessSession)}.
     */
    Uni<IEnterprise<?, ?>> isEnterpriseReady(Mutiny.StatelessSession session);
}
