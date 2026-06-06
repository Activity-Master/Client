package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple4;
import io.vertx.core.Vertx;
import lombok.extern.log4j.Log4j2;
import org.hibernate.FlushMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystem;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystemToken;

/**
 * Utility helpers for consistent Mutiny session lifecycle handling across Vert.x event consumers.
 * <p>
 * Session methods delegate to {@link Mutiny.SessionFactory#withTransaction} and
 * {@link Mutiny.SessionFactory#withSession} which internally manage connection acquisition,
 * context dispatch, and session lifecycle. This ensures the session is always opened on the
 * same event-loop thread as the underlying SQL pool connection — critical for Hibernate
 * Reactive's thread-affinity check (HR000069).
 * <p>
 * <strong>Important:</strong> Do NOT use {@code sessionFactory.openSession()} directly.
 * {@code openSession()} pins the session to the <em>calling</em> thread, which may differ
 * from the thread the SQL connection is bound to (especially when the pool has few active
 * connections). The factory-managed methods handle this correctly.
 *
 * @see Mutiny.SessionFactory#withTransaction
 * @see Mutiny.SessionFactory#withSession
 */
@Log4j2
public final class SessionUtils {

    private SessionUtils() {
    }

    /**
     * Run the provided work within a dedicated session and transaction.
     * <p>
     * Uses {@link Mutiny.SessionFactory#withTransaction(java.util.function.BiFunction)} which
     * internally acquires a pooled connection <strong>first</strong>, then opens the session
     * on the same event-loop thread that the connection is bound to. This guarantees that
     * the session's thread-pinning matches the SQL I/O thread, preventing
     * {@code HR000069: Detected use of the reactive Session from a different Thread}.
     * <p>
     * This is critical when the Vert.x SQL pool has few active connections (e.g. one):
     * all SQL responses fire on that connection's event-loop thread, so the session must
     * be opened on that same thread. {@code openSession()} pins to the <em>calling</em>
     * thread (the HTTP request thread), which differs from the connection thread.
     */
    public static <T> Uni<T> withSessionTx(Mutiny.SessionFactory sessionFactory,
                                           Function<Mutiny.Session, Uni<T>> work) {
        return sessionFactory.openSession().chain(session -> session.withTransaction(tx -> work.apply(session)
        ).eventually(session::close));
    }

    /**
     * Run the provided work within a Hibernate-managed stateless session and transaction.
     */
    public static <T> Uni<T> withStatelessSessionTx(Mutiny.SessionFactory sessionFactory,
                                                    Function<Mutiny.StatelessSession, Uni<T>> work) {
        return sessionFactory.openStatelessSession()
                .chain(session -> session
                        .withTransaction(tx -> work.apply(session))
                        .eventually(session::close));
    }

    /**
     * Run the provided work within a dedicated session (no explicit transaction).
     * <p>
     * Uses {@link Mutiny.SessionFactory#withSession(Function)} which internally
     * manages the Vert.x context and thread affinity properly.
     */
    public static <T> Uni<T> withSession(Mutiny.SessionFactory sessionFactory,
                                         Function<Mutiny.Session, Uni<T>> work) {
        return sessionFactory.withSession(work::apply);
    }

    /**
     * Run read-only work within a dedicated session (no transaction, no flush).
     * <p>
     * Optimised for pure reads (e.g. GraphQL data fetchers): the session is marked
     * {@link Mutiny.Session#setDefaultReadOnly(boolean) default read-only} so every entity it loads
     * is hydrated <em>without</em> a dirty-checking snapshot, and the flush mode is set to
     * {@link FlushMode#MANUAL} so Hibernate never auto-flushes before a query. This lowers CPU and
     * GC pressure on read-heavy paths. No transaction is opened and no {@code flush()}/{@code clear()}
     * is performed, because nothing is being written.
     * <p>
     * <strong>Do not</strong> perform writes through a session obtained here — pending changes will
     * not be flushed.
     */
    public static <T> Uni<T> withSessionReadOnly(Mutiny.SessionFactory sessionFactory,
                                                 Function<Mutiny.Session, Uni<T>> work) {
        return sessionFactory.withSession(session -> {
            session.setDefaultReadOnly(true);
            session.setFlushMode(FlushMode.MANUAL);
            return work.apply(session);
        });
    }

    /**
     * Run read work within a Hibernate-managed stateless session (no transaction).
     * <p>
     * A {@link Mutiny.StatelessSession} has no persistence context: no first-level cache, no
     * dirty checking and no auto-flush. It is the leanest option for projection/DTO reads that do
     * not need lazy navigation back through managed entities. Prefer this for high-volume leaf
     * GraphQL fetchers that map straight to a transport shape.
     */
    public static <T> Uni<T> withStatelessSession(Mutiny.SessionFactory sessionFactory,
                                                  Function<Mutiny.StatelessSession, Uni<T>> work) {
        return sessionFactory.withStatelessSession(work::apply);
    }

    /**
     * Runs work as a <em>named system using that system's own identity token</em>.
     * <p>
     * This is the canonical, blast-radius-minimising entry point: instead of every caller borrowing
     * the broadly-privileged ActivityMaster system token, each system resolves and runs under
     * <strong>its own</strong> security identity token (via
     * {@link IActivityMasterService#getISystemToken}). The supplied token therefore only grants the
     * access that <em>that</em> system has been scoped for in the security graph, so a compromised or
     * misbehaving caller can only reach what its own system is permitted to reach.
     *
     * @param enterpriseName The AM Enterprise Name (security scope)
     * @param systemName     The AM System Name that is performing the task — its own identity token is supplied to the work
     * @param consumer       The consumer to execute, receiving a tuple of (session, enterprise, system, tokens)
     * @return A reactive chain that can be executed with this session closed after completion.
     */
    public static Uni<Void> withSystemAndToken(String enterpriseName, String systemName, Consumer<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>> consumer) {
        return withSystemAndToken(enterpriseName, systemName, tuple -> {
            consumer.accept(tuple);
            return Uni.createFrom().voidItem();
        });
    }

    /**
     * Executes a consumer with the enterprise system and identity tokens.
     * <p>
     * <strong>Naming note:</strong> this is a backward-compatible alias for
     * {@link #withSystemAndToken(String, String, Consumer)} — it does <em>not</em> force the
     * ActivityMaster system token. The {@code systemName} you pass determines which system's own
     * identity token is supplied. Prefer {@link #withSystemAndToken} in new code to make the
     * per-system token intent explicit.
     *
     * @param enterpriseName The AM Enterprise Name
     * @param systemName     The AM System Name that is performing the task
     * @param consumer       The consumer to execute
     * @return A reactive chain that can be executed with this session closed after completion. The consumer receives a tuple of (session, enterprise, system, tokens).
     */
    public static Uni<Void> withActivityMaster(String enterpriseName, String systemName, Consumer<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>> consumer) {
        return withSystemAndToken(enterpriseName, systemName, consumer);
    }

    /**
     * Executes a reactive function as a <em>named system using that system's own identity token</em>.
     * Designed for use directly inside reactive chains.
     * <p>
     * This is the canonical, blast-radius-minimising entry point. The token array passed to the work
     * carries the {@code systemName} system's own identity token (resolved via
     * {@link IActivityMasterService#getISystemToken}), so downstream access checks evaluate only the
     * privileges that that specific system has been granted — never the broad ActivityMaster system
     * token unless {@code systemName} actually <em>is</em> the ActivityMaster system.
     *
     * @param enterpriseName The AM Enterprise Name (security scope)
     * @param systemName     The AM System Name that is performing the task — its own identity token is supplied
     * @param fn             Reactive function to execute which receives (session, enterprise, system, tokens)
     * @return A Uni of the function's result type. Session lifecycle and transaction are managed internally.
     */
    public static <T> Uni<T> withSystemAndToken(String enterpriseName, String systemName,
                                                java.util.function.Function<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>, Uni<T>> fn) {
        log.trace("Executing as system '{}' with its own identity token", systemName);
        Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        return withSessionTx(sessionFactory, session ->
                enterpriseService.getEnterprise(session, enterpriseName)
                        .chain(enterprise -> getISystem(session, systemName, enterprise)
                                .chain(system -> getISystemToken(session, systemName, enterprise)
                                        .chain(token -> fn.apply(Tuple4.of(session, enterprise, system, new UUID[]{token})))
                                        .chain(a -> {
                                            // Flush any pending changes to the database BEFORE clearing the
                                            // persistence context. session.clear() detaches everything and would
                                            // otherwise discard not-yet-flushed inserts/updates, so the surrounding
                                            // transaction would commit nothing (writes silently lost).
                                            return session.flush()
                                                    .invoke(session::clear)
                                                    .replaceWith(a);
                                        })
                                 )
                         )
        );
    }

    /**
     * Executes a reactive function with the enterprise system and identity tokens.
     * Designed for use directly inside reactive chains.
     * <p>
     * <strong>Naming note:</strong> this is a backward-compatible alias for
     * {@link #withSystemAndToken(String, String, java.util.function.Function)} — it does <em>not</em>
     * force the ActivityMaster system token. The {@code systemName} you pass determines which
     * system's own identity token is supplied. Prefer {@link #withSystemAndToken} in new code to make
     * the per-system token intent explicit.
     *
     * @param enterpriseName The AM Enterprise Name
     * @param systemName     The AM System Name that is performing the task
     * @param fn             Reactive function to execute which receives (session, enterprise, system, tokens)
     * @return A Uni of the function's result type. Session lifecycle and transaction are managed internally.
     */
    public static <T> Uni<T> withActivityMaster(String enterpriseName, String systemName,
                                                java.util.function.Function<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>, Uni<T>> fn) {
        return withSystemAndToken(enterpriseName, systemName, fn);
    }

    /**
     * Read-only variant of {@link #withSystemAndToken(String, String, java.util.function.Function)}
     * for pure-read paths such as GraphQL data fetchers — runs as the named system using
     * <em>that system's own identity token</em>, keeping the blast radius scoped to that system.
     * <p>
     * Resolves the same enterprise/system/identity-token context, but runs inside a
     * {@link #withSessionReadOnly read-only, no-transaction} session: entities are loaded without
     * dirty-checking snapshots, auto-flush is disabled ({@link FlushMode#MANUAL}) and there is no
     * trailing {@code flush()}/{@code clear()}. Because the session is {@code defaultReadOnly}, every
     * nested EntityAssist query inherits read-only execution automatically.
     * <p>
     * <strong>Reads only.</strong> Use {@link #withSystemAndToken} for any flow that writes.
     *
     * @param enterpriseName The AM Enterprise Name (security scope)
     * @param systemName     The AM System Name performing the read — its own identity token is supplied
     * @param fn             Reactive read function receiving (session, enterprise, system, tokens)
     * @return A Uni of the function's result type.
     */
    public static <T> Uni<T> withSystemAndTokenReadOnly(String enterpriseName, String systemName,
                                                        java.util.function.Function<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>, Uni<T>> fn) {
        log.trace("Executing read-only as system '{}' with its own identity token", systemName);
        Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        return withSessionReadOnly(sessionFactory, session ->
                enterpriseService.getEnterprise(session, enterpriseName)
                        .chain(enterprise -> getISystem(session, systemName, enterprise)
                                .chain(system -> getISystemToken(session, systemName, enterprise)
                                        .chain(token -> fn.apply(Tuple4.of(session, enterprise, system, new UUID[]{token})))
                                )
                        )
        );
    }

    /**
     * Read-only variant of {@link #withActivityMaster(String, String, java.util.function.Function)}
     * for pure-read paths such as GraphQL data fetchers.
     * <p>
     * <strong>Naming note:</strong> backward-compatible alias for
     * {@link #withSystemAndTokenReadOnly(String, String, java.util.function.Function)}; the
     * {@code systemName} you pass determines which system's own identity token is supplied. Prefer
     * {@link #withSystemAndTokenReadOnly} in new code.
     *
     * @param enterpriseName The AM Enterprise Name
     * @param systemName     The AM System Name performing the read
     * @param fn             Reactive read function receiving (session, enterprise, system, tokens)
     * @return A Uni of the function's result type.
     */
    public static <T> Uni<T> withActivityMasterReadOnly(String enterpriseName, String systemName,
                                                        java.util.function.Function<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>, Uni<T>> fn) {
        return withSystemAndTokenReadOnly(enterpriseName, systemName, fn);
    }

    /**
     * Context-aware variant of {@link #withActivityMaster(String, String, Function)} that resolves
     * the enterprise and caller identity from the
     * {@link ActivityMasterConfiguration call context} rather than from explicit arguments.
     * <p>
     * Resolution:
     * <ul>
     *   <li><b>Enterprise</b> — the call-scoped {@link ActivityMasterConfiguration#getEnterpriseId()
     *       enterprise id} (typically supplied by the REST/event-bus entry point for the current
     *       request) is used when present; otherwise it falls back to the process-wide
     *       {@link ActivityMasterConfiguration#applicationEnterpriseName startup enterprise name}
     *       resolved once at application start.</li>
     *   <li><b>Tokens</b> — the resolved system token is always supplied as element {@code [0]}; when a
     *       call-scoped {@link ActivityMasterConfiguration#getIdentityToken() identity token} is
     *       present it is appended as element {@code [1]}, so downstream access checks evaluate the
     *       caller's identity alongside the system identity.</li>
     * </ul>
     *
     * @param systemName the AM system performing the work
     * @param fn         reactive function receiving (session, enterprise, system, tokens)
     * @return a Uni of the function's result type
     */
    public static <T> Uni<T> withActivityMasterFromContext(String systemName,
                                                           Function<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>, Uni<T>> fn) {
        log.trace("Executing with activity master details resolved from call context");
        ActivityMasterConfiguration configuration = ActivityMasterConfiguration.get();
        UUID enterpriseId = configuration.getEnterpriseId();
        Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        return withSessionTx(sessionFactory, session -> {
            Uni<IEnterprise<?, ?>> enterpriseUni = enterpriseId != null
                    ? enterpriseService.getEnterprise(session, enterpriseId)
                    : enterpriseService.getEnterprise(session, ActivityMasterConfiguration.applicationEnterpriseName);
            return enterpriseUni
                    .chain(enterprise -> getISystem(session, systemName, enterprise)
                            .chain(system -> getISystemToken(session, systemName, enterprise)
                                    .chain(token -> fn.apply(Tuple4.of(session, enterprise, system,
                                            contextTokens(token, configuration.getIdentityToken()))))
                                    .chain(a -> session.flush()
                                            .invoke(session::clear)
                                            .replaceWith(a))
                            )
                    );
        });
    }

    /**
     * Context-aware variant that runs as the named system using <em>that system's own identity
     * token</em>, resolving the enterprise (and any caller identity token) from the
     * {@link ActivityMasterConfiguration call context}. Backward-compatible alias for
     * {@link #withActivityMasterFromContext(String, Function)} with a name that makes the
     * per-system-token intent explicit.
     *
     * @param systemName the AM system performing the work — its own identity token is supplied as element {@code [0]}
     * @param fn         reactive function receiving (session, enterprise, system, tokens)
     * @return a Uni of the function's result type
     */
    public static <T> Uni<T> withSystemAndTokenFromContext(String systemName,
                                                           Function<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>, Uni<T>> fn) {
        return withActivityMasterFromContext(systemName, fn);
    }

    /**
     * Resolves a single named system's own security identity token within an enterprise, opening and
     * closing a dedicated read-only session internally.
     * <p>
     * Use this when a caller only needs <em>another</em> system's scoped token (for example to stamp
     * security or to act on its behalf) without standing up a full {@link #withSystemAndToken} work
     * block — keeping the access scoped to that specific system rather than the broad ActivityMaster
     * system token.
     *
     * @param enterpriseName the AM enterprise name (security scope)
     * @param systemName     the AM system whose own identity token is required
     * @return a Uni emitting that system's identity token UUID
     */
    public static Uni<UUID> getSystemToken(String enterpriseName, String systemName) {
        Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        return withSessionReadOnly(sessionFactory, session ->
                enterpriseService.getEnterprise(session, enterpriseName)
                        .chain(enterprise -> getISystemToken(session, systemName, enterprise)));
    }

    /**
     * Builds the token array passed to downstream work: the system token first, with the call
     * context's identity token appended when present and distinct.
     */
    private static UUID[] contextTokens(UUID systemToken, UUID identityToken) {
        if (identityToken != null && !identityToken.equals(systemToken)) {
            return new UUID[]{systemToken, identityToken};
        }
        return new UUID[]{systemToken};
    }

    /**
     * Fire-and-forget: runs the given {@link Uni} on a <b>new duplicated Vert.x context</b>.
     * <p>
     * Hibernate Reactive's {@code withTransaction} uses the Vert.x context to track the
     * active session. When multiple fire-and-forget operations are subscribed from the same
     * request handler, they share the caller's context and collide — causing
     * "Illegal pop() with non-matching JdbcValuesSourceProcessingState" or HR000069 errors.
     * <p>
     * By dispatching each subscription onto its own duplicated context, each
     * {@code withTransaction} call gets a completely isolated session and connection.
     * <p>
     * Failures are logged at ERROR level but never propagated to the caller.
     *
     * @param uni         the reactive pipeline to execute (should use {@code withActivityMaster})
     * @param description a short label used in log messages (e.g. "event 123 classifications")
     */
    public static void fireAndForget(Uni<?> uni, String description) {
        Vertx vertx = Vertx.currentContext() != null
                ? Vertx.currentContext().owner()
                : IGuiceContext.get(Vertx.class);
        io.vertx.core.Context newCtx = vertx.getOrCreateContext();
        newCtx.runOnContext(v ->
                uni.subscribe().with(
                        success -> log.trace("Async operation completed: {}", description),
                        failure -> log.error("Async operation failed: {}: {}", description, failure.getMessage(), failure)
                )
        );
    }

}