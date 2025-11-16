package com.guicedee.activitymaster.fsdm.client.services;

/**
 * Reactivity Migration Checklist:
 * 
 * [✓] One action per Mutiny.Session at a time
 *     - All operations on a session are sequential
 *     - No parallel operations on the same session
 * 
 * [✓] Pass Mutiny.Session through the chain
 *     - All methods accept session as parameter
 *     - Session is passed to all dependent operations
 * 
 * [✓] No await() usage
 *     - Using reactive chains instead of blocking operations
 * 
 * [✓] Synchronous execution of reactive chains
 *     - All reactive chains execute synchronously
 *     - No fire-and-forget operations with subscribe().with()
 * 
 * [✓] No parallel operations on a session
 *     - Not using Uni.combine().all().unis() with operations that share the same session
 * 
 * [✓] No session/transaction creation in libraries
 *     - Sessions are passed in from the caller
 *     - No sessionFactory.withTransaction() in methods
 * 
 * See ReactivityMigrationGuide.md for more details on these rules.
 */

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;

/**
 * Interface for systems service.
 * This interface provides methods for managing systems.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface ISystemsService<J extends ISystemsService<J>> {
    String ActivityMasterSystemName = "Activity Master System";
    String ActivityMasterWebSystemName = "Activity Master Web";

    /**
     * Gets the Activity Master system.
     */
    Uni<ISystems<?, ?>> getActivityMaster(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Gets the Activity Master system.
     */
    Uni<ISystems<?, ?>> getActivityMaster(Mutiny.Session session, IEnterprise<?, ?> requestingSystem, UUID... identityToken);

    /**
     * Checks if a system exists.
     */
    Uni<Boolean> doesSystemExist(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, UUID... identityToken);

    /**
     * Finds a system by enterprise and system name.
     */
    Uni<ISystems<?, ?>> findSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, UUID... identityToken);

    /**
     * Finds a system by system and token.
     */
    Uni<ISystems<?, ?>> findSystem(Mutiny.Session session, ISystems<?, ?> system, String token, UUID... identityToken);

    /**
     * Registers a new system.
     */
    Uni<String> registerNewSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise, ISystems<?, ?> newSystem);

    /**
     * Creates a new system.
     */
    Uni<ISystems<?, ?>> create(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, String systemDesc, UUID... identityToken);

    /**
     * Creates a new system with history name.
     */
    Uni<ISystems<?, ?>> create(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, String systemDesc, String historyName, UUID... identityToken);

    /**
     * Gets the security identity token for a system.
     */
    Uni<UUID> getSecurityIdentityToken(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);
}