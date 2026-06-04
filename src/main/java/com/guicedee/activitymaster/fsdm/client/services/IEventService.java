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


/**
 * Service interface for managing events.
 * Events represent occurrences or activities within the system, often linked to other domains like parties or products.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IEventService<J extends IEventService<J>> {
    /**
     * The name of the Events system.
     */
    String EventSystemName = "Events System";

    /**
     * Gets a new, uninitialized event instance.
     *
     * @return A new event instance
     */
    Uni<IEvent<?, ?>> get();

    /**
     * Finds an event by its unique ID.
     *
     * @param session The Mutiny session to use
     * @param id      The UUID of the event
     * @return A Uni emitting the found event
     */
    Uni<IEvent<?, ?>> find(Mutiny.Session session, UUID id);

    /**
     * Creates a new event of a specific type.
     *
     * @param session        The Mutiny session to use
     * @param eventType      The type of event to create
     * @param system         The system creating the event
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created event
     */
    Uni<IEvent<?, ?>> createEvent(Mutiny.Session session, String eventType, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new event type using an enum.
     *
     * @param session        The Mutiny session to use
     * @param eventType      The event type enum
     * @param system         The system creating the type
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created event type
     */
    default Uni<IEventType<?, ?>> createEventType(Mutiny.Session session, Enum<?> eventType, ISystems<?, ?> system, UUID... identityToken) {
        return createEventType(session, eventType.toString(), system, identityToken);
    }

    /**
     * Creates a new event with a specific key and type.
     *
     * @param session        The Mutiny session to use
     * @param eventType      The type of event to create
     * @param key            The UUID key for the event
     * @param system         The system creating the event
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created event
     */
    Uni<IEvent<?, ?>> createEvent(Mutiny.Session session, String eventType, UUID key, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Creates a new event type by name.
     *
     * @param session        The Mutiny session to use
     * @param eventType      The name of the event type
     * @param system         The system creating the type
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created event type
     */
    Uni<IEventType<?, ?>> createEventType(Mutiny.Session session, String eventType, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds an event type by name.
     *
     * @param session        The Mutiny session to use
     * @param eventType      The name of the event type
     * @param system         The system searching for the type
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the found event type
     */
    Uni<IEventType<?, ?>> findEventType(Mutiny.Session session, String eventType, ISystems<?, ?> system, UUID... identityToken);

    // --- Cross-domain searchable queries (EventX<DomainType>) ---

    /**
     * Finds events by classification name and value.
     *
     * @param session            The Mutiny session to use
     * @param classificationName The name of the classification
     * @param value              The classification value
     * @param systems            The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds events by classification and parent event.
     *
     * @param session            The Mutiny session to use
     * @param classificationName The name of the classification
     * @param withParent         The parent event constraint
     * @param value              The classification value
     * @param systems            The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByClassification(Mutiny.Session session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds events by classification value greater than the specified value.
     *
     * @param session            The Mutiny session to use
     * @param classificationName The name of the classification
     * @param withParent         The parent event constraint
     * @param value              The classification value
     * @param systems            The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationGT(Mutiny.Session session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds events by classification value greater than or equal to the specified value.
     *
     * @param session            The Mutiny session to use
     * @param classificationName The name of the classification
     * @param withParent         The parent event constraint
     * @param value              The classification value
     * @param systems            The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationGTE(Mutiny.Session session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds events by classification value less than the specified value.
     *
     * @param session            The Mutiny session to use
     * @param classificationName The name of the classification
     * @param withParent         The parent event constraint
     * @param value              The classification value
     * @param systems            The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationLT(Mutiny.Session session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds events by classification value less than or equal to the specified value.
     *
     * @param session            The Mutiny session to use
     * @param classificationName The name of the classification
     * @param withParent         The parent event constraint
     * @param value              The classification value
     * @param systems            The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationLTE(Mutiny.Session session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /**
     * Finds an event associated with a specific involved party and classification.
     *
     * @param session            The Mutiny session to use
     * @param involvedParty      The involved party
     * @param classificationName The classification name
     * @param value              The classification value
     * @param system             The system searching for the event
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting the found event
     */
    Uni<IEvent<?, ?>> findEventByInvolvedParty(Mutiny.Session session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds events associated with a specific involved party and classification.
     *
     * @param session            The Mutiny session to use
     * @param involvedParty      The involved party
     * @param classificationName The classification name
     * @param value              The classification value
     * @param system             The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds events associated with an involved party within a date range starting from {@code startDate}.
     *
     * @param session            The Mutiny session to use
     * @param involvedParty      The involved party
     * @param classificationName The classification name
     * @param value              The classification value
     * @param startDate          The start date constraint
     * @param system             The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, LocalDateTime startDate, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds events associated with an involved party within a specific date range.
     *
     * @param session            The Mutiny session to use
     * @param involvedParty      The involved party
     * @param classificationName The classification name
     * @param value              The classification value
     * @param startDate          The start date constraint
     * @param endDate            The end date constraint
     * @param system             The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, LocalDateTime startDate, LocalDateTime endDate, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds an event associated with a specific resource item and classification.
     *
     * @param session            The Mutiny session to use
     * @param resourceItem       The resource item
     * @param classificationName The classification name
     * @param value              The classification value
     * @param system             The system searching for the event
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting the found event
     */
    Uni<IEvent<?, ?>> findEventByResourceItem(Mutiny.Session session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds an event associated with a specific arrangement and classification.
     *
     * @param session            The Mutiny session to use
     * @param arrangement        The arrangement
     * @param classificationName The classification name
     * @param value              The classification value
     * @param system             The system searching for the event
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting the found event
     */
    Uni<IEvent<?, ?>> findEventByArrangement(Mutiny.Session session, IArrangement<?, ?> arrangement, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds an event associated with a specific product and classification.
     *
     * @param session            The Mutiny session to use
     * @param product            The product
     * @param classificationName The classification name
     * @param value              The classification value
     * @param system             The system searching for the event
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting the found event
     */
    Uni<IEvent<?, ?>> findEventByProduct(Mutiny.Session session, IProduct<?, ?> product, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds events associated with a specific rules instance and classification.
     *
     * @param session            The Mutiny session to use
     * @param rules              The rules instance
     * @param classificationName The classification name
     * @param value              The classification value
     * @param system             The system searching for events
     * @param identityToken      Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findEventsByRules(Mutiny.Session session, IRules<?, ?> rules, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds all events of a given type.
     *
     * @param session        The Mutiny session to use
     * @param eventType      The name of the event type
     * @param system         The system searching for events
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting a list of found events
     */
    Uni<List<IEvent<?, ?>>> findAll(Mutiny.Session session, String eventType, ISystems<?, ?> system, UUID... identityToken);

}
