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

}
