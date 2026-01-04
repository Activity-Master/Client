package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEventType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


public interface IEventService<J extends IEventService<J>>
{
	String EventSystemName =  "Events System";

	Uni<IEvent<?,?>> get();

	Uni<IEvent<?, ?>> find(Mutiny.Session session, UUID id);

	Uni<IEvent<?,?>> createEvent(Mutiny.Session session, String eventType, ISystems<?,?> system, UUID... identityToken);

	default Uni<IEventType<?,?>> createEventType(Mutiny.Session session, Enum<?> eventType, ISystems<?,?> system, UUID... identityToken)
	{
		return createEventType(session, eventType.toString(), system, identityToken);
	}

	Uni<IEvent<?, ?>> createEvent(Mutiny.Session session, String eventType, UUID key, ISystems<?, ?> system, UUID... identityToken);

	Uni<IEventType<?,?>> createEventType(Mutiny.Session session, String eventType, ISystems<?,?> system, UUID... identityToken);

	Uni<IEventType<?,?>> findEventType(Mutiny.Session session, String eventType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Resolve EventType ID (UUID) by enterprise and name, using cache and ActiveFlag visible range with SCD window.
	 * Contract: never returns null; lets NoResultException propagate on misses.
	 */
	default Uni<java.util.UUID> resolveEventTypeIdByName(Mutiny.Session session, java.util.UUID enterpriseId, String eventTypeName) {
		return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
				.getEventTypeId(session, enterpriseId, eventTypeName, (sess, name) -> {
					var afService = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService.class);
					return afService.getVisibleRangeAndUpIds(sess, enterpriseId)
							.flatMap(visibleIds -> {
								String sql = "select eventtypeid from event.eventtype " +
										"where enterpriseid = :ent and eventtypename = :name " +
										"and (effectivefromdate <= current_timestamp) " +
										"and (effectivetodate > current_timestamp) " +
										"and activeflagid in (:visibleIds)";
								return sess.createNativeQuery(sql)
										.setParameter("ent", enterpriseId)
										.setParameter("name", name)
										.setParameter("visibleIds", visibleIds)
										.getSingleResult()
										.map(result -> (java.util.UUID) result);
							});
				});
	}

}
