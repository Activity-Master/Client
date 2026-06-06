package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base;

//import com.entityassist.services.entities.ISCDEntity;

import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD;
import com.guicedee.activitymaster.fsdm.client.services.builders.ISCDEntity;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.time.Duration;
import java.util.UUID;

/**
 * Base interface for all warehouse tables in Activity Master.
 * Provides support for SCD (Slowly Changing Dimension) and record expiration.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 * @param <I> The identifier type
 */
public interface IWarehouseBaseTable<
                                            J extends IWarehouseBaseTable<J, Q, I>,
                                            Q extends IQueryBuilderDefault<Q, J, I>,
                                            I extends UUID>
        extends ISCDEntity<J, Q, I>, Serializable
{

    /**
     * Expires the current record with immediate effect, opening a dedicated session.
     * <p>
     * Prefer {@link #expire(Mutiny.Session)} when an active session is already available
     * to avoid opening a second connection and to keep the work inside the caller's transaction.
     *
     * @return the expired entity
     */
    default Uni<J> expire()
    {
        return expire(Duration.ZERO);
    }

    /**
     * Expires the current record with immediate effect using the provided session.
     * <p>
     * This is the preferred overload when called from within an existing
     * {@code withSessionTx} block because it reuses the caller's session and transaction.
     *
     * @param session the active Mutiny session to use
     * @return the expired entity
     */
    default Uni<J> expire(Mutiny.Session session)
    {
        return expire(session, Duration.ZERO);
    }

    /**
     * Expires the current record after the given duration, opening a dedicated session and transaction.
     * <p>
     * Prefer {@link #expire(Mutiny.Session, Duration)} when an active session is already available.
     *
     * @param duration The duration to add to the current time for expiry
     * @return The expired entity
     */
    @SuppressWarnings("unchecked")
    default Uni<J> expire(Duration duration)
    {
        J me = (J) this;
        var log = LogManager.getLogger(getClass().getSimpleName());
        log.debug("🕒 Starting expiration for entity: {} (ID: {}) with duration: {}",
                        me.getClass().getSimpleName(), me.getId(), duration);

        Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);

        return sessionFactory.openSession()
                .chain(session ->
                        session.withTransaction(tx -> expireInternal(session, duration))
                        .eventually(session::close)
                )
                .onFailure()
                .invoke(err ->
                        log.warn("❌ Failed to expire entity: {} (ID: {}) - {}",
                                me.getClass().getSimpleName(), me.getId(), err.getMessage(), err)
                );
    }

    /**
     * Expires the current record after the given duration using the provided session.
     * <p>
     * The caller is responsible for transaction and session lifecycle management.
     *
     * @param session  the active Mutiny session to use
     * @param duration how far in the future to set the expiry (Duration.ZERO = immediate)
     * @return the expired entity
     */
    default Uni<J> expire(Mutiny.Session session, Duration duration)
    {
        return expireInternal(session, duration)
                .onFailure()
                .invoke(err -> {
                    var log = LogManager.getLogger(getClass().getSimpleName());
                    log.warn("❌ Failed to expire entity: {} (ID: {}) - {}",
                            getClass().getSimpleName(), getId(), err.getMessage(), err);
                });
    }

    /**
     * Internal implementation shared by all expire overloads.
     *
     * @param session  The session
     * @param duration The duration
     * @return The updated entity
     */
    @SuppressWarnings("unchecked")
    private Uni<J> expireInternal(Mutiny.Session session, Duration duration)
    {
        J me = (J) this;
        var log = LogManager.getLogger(getClass().getSimpleName());
        var newExpiry = IQueryBuilderSCD
                .convertToUTCDateTime(com.entityassist.RootEntity.getNow())
                .plus(duration);

        me.setEffectiveToDate(newExpiry);
        log.trace("📆 Setting EffectiveToDate = {} for entity: {}", newExpiry, me.getId());
        return me.builder(session)
                .update()
                .invoke(() -> log.debug("✅ Entity expired and updated in DB: {}", me.getId()));
    }

    /**
     * Checks if the entity is a "fake" or transient entity (no ID assigned yet).
     *
     * @return true if ID is null
     */
    default boolean isFake()
    {
        return getId() == null;
    }

}
