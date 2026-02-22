package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import com.guicedee.vertx.spi.VertXPreStartup;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple4;
import io.vertx.core.Context;
import io.vertx.core.internal.VertxInternal;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystem;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystemToken;

/**
 * Utility helpers for consistent Mutiny session lifecycle handling across Vert.x event consumers.
 * Ensures sessions are explicitly opened and always closed, even on failure.
 */
@Log4j2
public final class SessionUtils {

    private SessionUtils() {
    }

    /**
     * Run the provided work within a Hibernate-managed session and transaction.
     * <p>
     * Uses {@link Mutiny.SessionFactory#withTransaction} which properly associates
     * the session with the current Vert.x local context.  This is required for
     * Hibernate Reactive to resolve associations during second-level cache assembly.
     * Using {@code openSession()} manually does NOT register the session in the
     * context and causes {@code UnexpectedAccessToTheDatabase} on cached entity loads.
     */
    public static <T> Uni<T> withSessionTx(Mutiny.SessionFactory sessionFactory,
                                           Function<Mutiny.Session, Uni<T>> work) {
        return sessionFactory.openSession()
                .chain(session -> session
                        .withTransaction(tx -> work.apply(session))
                        .chain(ses -> {
                            return Uni.createFrom().item(ses);
                        })
                        .eventually(session::close));
    }

    /**
     * Run the provided work within a Hibernate-managed stateless session and transaction.
     */
    public static <T> Uni<T> withStatelessSessionTx(Mutiny.SessionFactory sessionFactory,
                                                    Function<Mutiny.StatelessSession, Uni<T>> work) {
        return sessionFactory.withStatelessTransaction((session, tx) -> work.apply(session));
    }

    /**
     * Run the provided work within a Hibernate-managed session (no explicit transaction).
     */
    public static <T> Uni<T> withSession(Mutiny.SessionFactory sessionFactory,
                                         Function<Mutiny.Session, Uni<T>> work) {
        return sessionFactory.withSession(work);
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
                                )
                        ));
    }

}