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
     *
     * @param session        The Mutiny session to use
     * @param system         The system making the request
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the Activity Master system
     */
    Uni<ISystems<?, ?>> getActivityMaster(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Gets the Activity Master system for a specific enterprise.
     *
     * @param session           The Mutiny session to use
     * @param requestingSystem  The enterprise requesting the system
     * @param identityToken     Optional security identity tokens
     * @return A Uni emitting the Activity Master system
     */
    Uni<ISystems<?, ?>> getActivityMaster(Mutiny.Session session, IEnterprise<?, ?> requestingSystem, UUID... identityToken);

    /**
     * Stateless-safe resolution of the Activity Master <em>system id</em>.
     * <p>
     * Returns the PK only (a scalar) rather than a managed {@code Systems} entity: that entity is
     * {@code @Cacheable} with eager {@code @ManyToOne} associations, which Hibernate Reactive's
     * stateless session cannot hydrate (criteria-query LoadContexts underflow / L2-cache reactive
     * association assembly failure). Stateless callers use the id for tokens/FKs; callers needing the
     * managed entity must use a {@link Mutiny.Session}.
     *
     * @param session          The stateless session to use
     * @param requestingSystem The enterprise requesting the system
     * @param identityToken    Optional security identity tokens
     * @return A Uni emitting the Activity Master system id
     */
    Uni<UUID> getActivityMasterId(Mutiny.StatelessSession session, IEnterprise<?, ?> requestingSystem, UUID... identityToken);

    /**
     * Stateless-safe resolution of the Activity Master system as a <em>prepped, detached</em>
     * {@code Systems} entity.
     * <p>
     * Rather than hydrating the managed entity (impossible on a stateless session because {@code Systems}
     * is {@code @Cacheable} with eager {@code @ManyToOne} associations), this projects the row's own
     * scalar columns ({@code id, name, description, systemHistoryName}) through a JPA constructor result
     * and "preps" a fresh detached instance, wiring the {@code enterprise} reference from the supplied
     * parameter (already in hand — no extra read). The eager FK associations remain {@code null}; the
     * entity carries exactly the identity + descriptive columns needed to drive FKs, tokens and logging
     * downstream on the same stateless session.
     *
     * @param session          The stateless session to use
     * @param requestingSystem The enterprise requesting the system
     * @param identityToken    Optional security identity tokens
     * @return A Uni emitting the prepped, detached Activity Master system
     */
    Uni<ISystems<?, ?>> getActivityMaster(Mutiny.StatelessSession session, IEnterprise<?, ?> requestingSystem, UUID... identityToken);

    /**
     * Checks if a system with the specified name exists within an enterprise.
     *
     * @param session        The Mutiny session to use
     * @param enterprise     The enterprise to search within
     * @param systemName     The name of the system to check
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting true if the system exists, false otherwise
     */
    Uni<Boolean> doesSystemExist(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, UUID... identityToken);

    /**
     * Stateless-session variant of {@link #doesSystemExist(Mutiny.Session, IEnterprise, String, UUID...)}.
     *
     * @param session        The stateless session to use
     * @param enterprise     The enterprise to search within
     * @param systemName     The name of the system to check
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting true if the system exists, false otherwise
     */
    Uni<Boolean> doesSystemExist(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String systemName, UUID... identityToken);

    /**
     * Finds a system by enterprise and system name.
     *
     * @param session        The Mutiny session to use
     * @param enterprise     The enterprise to search within
     * @param systemName     The name of the system to find
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the found system
     */
    Uni<ISystems<?, ?>> findSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, UUID... identityToken);

    /**
     * Stateless-safe resolution of a system <em>id</em> by name within an enterprise.
     * <p>
     * Returns the PK only (a scalar projection) — never a managed {@code Systems} entity (see
     * {@link #getActivityMasterId(Mutiny.StatelessSession, IEnterprise, UUID...)} for why entity
     * hydration is not possible on a stateless session for this type).
     *
     * @param session        The stateless session to use
     * @param enterprise     The enterprise to search within
     * @param systemName     The name of the system to find
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the found system id
     */
    Uni<UUID> findSystemId(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String systemName, UUID... identityToken);

    /**
     * Stateless-safe resolution of a system by name within an enterprise as a <em>prepped, detached</em>
     * {@code Systems} entity.
     * <p>
     * Same mechanism as {@link #getActivityMaster(Mutiny.StatelessSession, IEnterprise, UUID...)}: the
     * row's own scalar columns are projected through a JPA constructor result to build a fresh detached
     * instance (eager FK associations left {@code null}), with the {@code enterprise} reference set from
     * the supplied parameter. Use this when a stateless caller needs the system's descriptive columns or
     * an entity reference (not only its id).
     *
     * @param session        The stateless session to use
     * @param enterprise     The enterprise to search within
     * @param systemName     The name of the system to find
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the prepped, detached system
     */
    Uni<ISystems<?, ?>> findSystem(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String systemName, UUID... identityToken);

    /**
     * Finds a system by system object and token.
     *
     * @param session        The Mutiny session to use
     * @param system         The system object
     * @param token          The token to search for
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the found system
     */
    Uni<ISystems<?, ?>> findSystem(Mutiny.Session session, ISystems<?, ?> system, String token, UUID... identityToken);

    /**
     * Registers a new system for an enterprise.
     *
     * @param session    The Mutiny session to use
     * @param enterprise The enterprise to register the system for
     * @param newSystem  The system object to register
     * @return A Uni emitting the registration result string
     */
    Uni<String> registerNewSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise, ISystems<?, ?> newSystem);

    /**
     * Stateless variant of {@link #registerNewSystem(Mutiny.Session, IEnterprise, ISystems)} — provisions the
     * new system's identity security token + the Systems-group token, links them, tags the system with its
     * {@code SystemIdentity} classification, secures both tokens, and creates the system involved party —
     * entirely on a {@link Mutiny.StatelessSession} (prepped reads + {@code session.insert} + the stateless
     * default-security path).
     */
    Uni<String> registerNewSystem(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ISystems<?, ?> newSystem);

    /**
     * Creates a new system within an enterprise.
     *
     * @param session        The Mutiny session to use
     * @param enterprise     The enterprise to create the system for
     * @param systemName     The name of the new system
     * @param systemDesc     The description of the new system
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created system
     */
    Uni<ISystems<?, ?>> create(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, String systemDesc, UUID... identityToken);

    /**
     * Creates a new system with a specific history name.
     *
     * @param session        The Mutiny session to use
     * @param enterprise     The enterprise to create the system for
     * @param systemName     The name of the new system
     * @param systemDesc     The description of the new system
     * @param historyName    The history name for the system
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created system
     */
    Uni<ISystems<?, ?>> create(Mutiny.Session session, IEnterprise<?, ?> enterprise, String systemName, String systemDesc, String historyName, UUID... identityToken);

    /**
     * Stateless find-or-create of a {@code Systems} row (no security writes — the system row is created
     * lean; classifications/security are provisioned by the later install phases). Returns a prepped
     * detached system when it already exists, otherwise inserts a new one on the
     * {@link Mutiny.StatelessSession}.
     */
    Uni<ISystems<?, ?>> create(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String systemName, String systemDesc, UUID... identityToken);

    /** Stateless variant of {@link #create(Mutiny.Session, IEnterprise, String, String, String, UUID...)}. */
    Uni<ISystems<?, ?>> create(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String systemName, String systemDesc, String historyName, UUID... identityToken);

    /**
     * Gets the security identity token for a system.
     *
     * @param session        The Mutiny session to use
     * @param system         The system to get the token for
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the security identity token UUID
     */
    Uni<UUID> getSecurityIdentityToken(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Stateless-safe resolution of a system's security identity token UUID.
     * <p>
     * Reads the {@code SystemIdentity} relationship-classification's stored value as a <em>scalar</em>
     * projection of the link row's {@code Value} column — never hydrating the {@code @Cacheable} link
     * entity (which carries eager FK associations) — so it is safe on a {@link Mutiny.StatelessSession}.
     * Composes on the prepped stateless system + prepped classification.
     *
     * @param session        The stateless session to use
     * @param system         The system to get the token for
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the security identity token UUID
     */
    Uni<UUID> getSecurityIdentityToken(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Resolves a Systems ID (UUID) by its unique name within an enterprise using a lightweight native SQL lookup
     * with a small in-memory cache to reduce database load.
     */
    default Uni<UUID> resolveSystemIdByName(Mutiny.Session session, UUID enterpriseId, String systemName) {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getSystemId(session, enterpriseId, systemName, (sess, name) -> {
                    String sql = "select systemid from dbo.systems where enterpriseid = :ent and systemname = :name " +
                                 "and (effectivefromdate <= current_timestamp) " +
                                 "and (effectivetodate > current_timestamp) " +
                                 "and activeflagid = (select activeflagid from dbo.activeflag where enterpriseid = :ent and activeflagname = 'Active')";
                    return sess.createNativeQuery(sql)
                               .setParameter("ent", enterpriseId)
                               .setParameter("name", name)
                               .getSingleResult()
                               .map(result -> (UUID) result);
                });
    }

    /**
     * Stateless variant of {@link #resolveSystemIdByName(Mutiny.Session, UUID, String)}.
     * <p>
     * Resolves the systems id via a scalar native-SQL lookup (never hydrating the {@code @Cacheable}
     * {@code Systems} entity), so it is safe on a {@link Mutiny.StatelessSession}. Shares the same
     * {@link com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache} key space as the managed
     * resolver, so a value cached by either path is reused by both.
     */
    default Uni<UUID> resolveSystemIdByName(Mutiny.StatelessSession session, UUID enterpriseId, String systemName) {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getSystemId(session, enterpriseId, systemName, (sess, name) -> {
                    String sql = "select systemid from dbo.systems where enterpriseid = :ent and systemname = :name " +
                                 "and (effectivefromdate <= current_timestamp) " +
                                 "and (effectivetodate > current_timestamp) " +
                                 "and activeflagid = (select activeflagid from dbo.activeflag where enterpriseid = :ent and activeflagname = 'Active')";
                    return sess.createNativeQuery(sql)
                               .setParameter("ent", enterpriseId)
                               .setParameter("name", name)
                               .getSingleResult()
                               .map(result -> (UUID) result);
                });
    }
}