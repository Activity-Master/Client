package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple4;
import lombok.extern.log4j.Log4j2;
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
        return sessionFactory.withTransaction((session, tx) -> work.apply(session));
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
        return sessionFactory.withSession(session -> work.apply(session));
    }

    /**
     * Executes a consumer with the enterprise system and identity tokens.
     *
     * @param enterpriseName The AM Enterprise Name
     * @param systemName     The AM System Name that is performing the task
     * @param consumer       The consumer to execute
     * @return A reactive chain that can be executed with this session closed after completion. The consumer receives a tuple of (session, enterprise, system, tokens).
     */
    public static Uni<Void> withActivityMaster(String enterpriseName, String systemName, Consumer<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>> consumer) {
        return withActivityMaster(enterpriseName, systemName, tuple -> {
            consumer.accept(tuple);
            return Uni.createFrom().voidItem();
        });
    }

    /**
     * Executes a reactive function with the enterprise system and identity tokens.
     * Designed for use directly inside reactive chains.
     *
     * @param enterpriseName The AM Enterprise Name
     * @param systemName     The AM System Name that is performing the task
     * @param fn             Reactive function to execute which receives (session, enterprise, system, tokens)
     * @return A Uni of the function's result type. Session lifecycle and transaction are managed internally.
     */
    public static <T> Uni<T> withActivityMaster(String enterpriseName, String systemName,
                                                java.util.function.Function<Tuple4<Mutiny.Session, IEnterprise<?, ?>, ISystems<?, ?>, UUID[]>, Uni<T>> fn) {
        log.trace("Executing with activity master details");
        Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        return withSessionTx(sessionFactory, session ->
                enterpriseService.getEnterprise(session, enterpriseName)
                        .chain(enterprise -> getISystem(session, systemName, enterprise)
                                .chain(system -> getISystemToken(session, systemName, enterprise)
                                        .chain(token -> fn.apply(Tuple4.of(session, enterprise, system, new UUID[]{token})))
                                        .chain(a -> {
                                            session.clear();
                                            return Uni.createFrom().item(a);
                                        })
                                )
                        )
        );
    }

    /**
     * Fire-and-forget: subscribes to a {@link Uni} directly on the current thread.
     * <p>
     * Each {@code withActivityMaster} Uni opens its own session and transaction, so there is
     * no shared session state to protect. The session's lifecycle is fully contained within
     * the Uni chain, and the SQL client pool will dispatch responses on the correct event-loop
     * thread that the underlying connection is bound to.
     * <p>
     * DO NOT wrap the subscription in a new event-loop context ({@code createEventLoopContext})
     * — that assigns a random Netty thread which differs from the SQL connection's thread,
     * causing Hibernate Reactive HR000069 (session used from different thread).
     * <p>
     * Failures are logged at ERROR level but never propagated to the caller.
     *
     * @param uni         the reactive pipeline to execute
     * @param description a short label used in log messages (e.g. "event 123 relationship persistence")
     */
    public static void fireAndForget(Uni<?> uni, String description) {
        uni.subscribe().with(
                success -> log.trace("Async operation completed: {}", description),
                failure -> log.error("Async operation failed: {}: {}", description, failure.getMessage(), failure)
        );
    }

}