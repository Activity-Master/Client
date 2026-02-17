package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEventType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products.IProduct;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRules;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.List;
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

 // --- Cross-domain searchable queries (EventX<DomainType>) ---

 // By Classification (equals)
 Uni<List<IEvent<?,?>>> findEventsByClassification(Mutiny.Session session, String classificationName, String value, ISystems<?,?> systems, UUID... identityToken);

 // By Classification with parent Event constraint
 Uni<List<IEvent<?,?>>> findEventsByClassification(Mutiny.Session session, String classificationName, IEvent<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

 // By Classification with comparison operators
 Uni<List<IEvent<?,?>>> findEventsByClassificationGT(Mutiny.Session session, String classificationName, IEvent<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);
 Uni<List<IEvent<?,?>>> findEventsByClassificationGTE(Mutiny.Session session, String classificationName, IEvent<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);
 Uni<List<IEvent<?,?>>> findEventsByClassificationLT(Mutiny.Session session, String classificationName, IEvent<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);
 Uni<List<IEvent<?,?>>> findEventsByClassificationLTE(Mutiny.Session session, String classificationName, IEvent<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

 // By Involved Party (single)
 Uni<IEvent<?,?>> findEventByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);
 // By Involved Party (lists, with/without date ranges)
 Uni<List<IEvent<?,?>>> findEventsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);
 Uni<List<IEvent<?,?>>> findEventsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, ISystems<?,?> system, UUID... identityToken);
 Uni<List<IEvent<?,?>>> findEventsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, LocalDateTime endDate, ISystems<?,?> system, UUID... identityToken);

 // By Resource Item
 Uni<IEvent<?,?>> findEventByResourceItem(Mutiny.Session session, IResourceItem<?,?> resourceItem, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

 // By Arrangement
 Uni<IEvent<?,?>> findEventByArrangement(Mutiny.Session session, IArrangement<?,?> arrangement, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

 // By Product
 Uni<IEvent<?,?>> findEventByProduct(Mutiny.Session session, IProduct<?,?> product, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

 // By Rules (instance)
 Uni<List<IEvent<?,?>>> findEventsByRules(Mutiny.Session session, IRules<?,?> rules, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

 // By Event Type (all events of a given type)
 Uni<List<IEvent<?,?>>> findAll(Mutiny.Session session, String eventType, ISystems<?,?> system, UUID... identityToken);

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
