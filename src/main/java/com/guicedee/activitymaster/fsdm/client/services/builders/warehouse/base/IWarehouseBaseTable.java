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

public interface IWarehouseBaseTable<
                                            J extends IWarehouseBaseTable<J, Q, I>,
                                            Q extends IQueryBuilderDefault<Q, J, I>,
                                            I extends UUID>
        extends ISCDEntity<J, Q, I>, Serializable
{

    /**
     * Expires with immediate effect
     * @return
     */
    default Uni<J> expire()
    {
        return expire(Duration.ZERO);
    }

    @SuppressWarnings("unchecked")
    default Uni<J> expire(Duration duration)
    {
        J me = (J) this;
        var log = LogManager.getLogger(getClass().getSimpleName());
        LogManager.getLogger(getClass().getSimpleName())
                .debug("🕒 Starting expiration for entity: {} (ID: {}) with duration: {}",
                        me.getClass()
                                .getSimpleName(),
                        me.getId(),
                        duration);

        Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);

        return sessionFactory.withSession(session ->
                                                  session.withTransaction(tx -> {
                                                      var newExpiry = IQueryBuilderSCD
                                                                              .convertToUTCDateTime(com.entityassist.RootEntity.getNow())
                                                                              .plus(duration);

                                                      me.setEffectiveToDate(newExpiry);
                                                      log.trace("📆 Setting EffectiveToDate = {} for entity: {}", newExpiry, me.getId());
                                                      return me.builder(session)
                                                                     .update()
                                                                     .invoke(() -> log.debug("✅ Entity expired and updated in DB: {}", me.getId()));
                                                  })
                )
                       .onFailure()
                       .invoke(err ->
                                       log.warn("❌ Failed to expire entity: {} (ID: {}) - {}",
                                               me.getClass()
                                                       .getSimpleName(),
                                               me.getId(),
                                               err.getMessage(),
                                               err)
                       );
    }

    default boolean isFake()
    {
        return getId() == null;
    }

}
