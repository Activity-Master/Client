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
     * Stateless variant of {@link #createEvent(Mutiny.StatelessSession, String, ISystems, UUID...)} — provisions the
     * event (and its event-type link) entirely on a {@link Mutiny.StatelessSession} via {@code session.insert}
     * + the stateless default-security path. World-readable (public) security matrix.
     *
     * @param session        The stateless session to use
     * @param eventType      The type of event to create
     * @param system         The system creating the event
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created event
     */
    Uni<IEvent<?, ?>> createEvent(Mutiny.StatelessSession session, String eventType, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Stateless variant of {@link #createEvent(Mutiny.StatelessSession, String, UUID, ISystems, UUID...)} (explicit key).
     *
     * @param session        The stateless session to use
     * @param eventType      The type of event to create
     * @param key            The UUID key for the event, or {@code null} to generate one
     * @param system         The system creating the event
     * @param identityToken  Optional security identity tokens
     * @return A Uni emitting the created event
     */
    Uni<IEvent<?, ?>> createEvent(Mutiny.StatelessSession session, String eventType, UUID key, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless scope-restricted variant of {@link #createEventScopeRestricted(Mutiny.StatelessSession, String, UUID, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, ISystems, UUID...)}. */
    Uni<IEvent<?, ?>> createEventScopeRestricted(Mutiny.StatelessSession session, String eventType, UUID key,
            com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
            ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #createEventType(Mutiny.StatelessSession, String, ISystems, UUID...)} — find-or-insert. */
    Uni<IEventType<?, ?>> createEventType(Mutiny.StatelessSession session, String eventType, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless Enum convenience over {@link #createEventType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
    default Uni<IEventType<?, ?>> createEventType(Mutiny.StatelessSession session, Enum<?> eventType, ISystems<?, ?> system, UUID... identityToken) {
        return createEventType(session, eventType.toString(), system, identityToken);
    }

    /** Stateless "fetch ids/scalars + prep" variant of {@link #findEventType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
    Uni<IEventType<?, ?>> findEventType(Mutiny.StatelessSession session, String eventType, ISystems<?, ?> system, UUID... identityToken);

    // --- Cross-domain searchable queries (EventX<DomainType>) ---

    // ---- Stateless finder twins ----

    /** Stateless variant of {@link #find(Mutiny.StatelessSession, UUID)}. */
    Uni<IEvent<?, ?>> find(Mutiny.StatelessSession session, UUID id);

    /** Stateless variant of {@link #findEventsByClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByClassification(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?, ?> systems, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByClassification(Mutiny.StatelessSession, String, IEvent, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByClassification(Mutiny.StatelessSession session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByClassificationGT(Mutiny.StatelessSession, String, IEvent, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationGT(Mutiny.StatelessSession session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByClassificationGTE(Mutiny.StatelessSession, String, IEvent, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationGTE(Mutiny.StatelessSession session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByClassificationLT(Mutiny.StatelessSession, String, IEvent, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationLT(Mutiny.StatelessSession session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByClassificationLTE(Mutiny.StatelessSession, String, IEvent, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByClassificationLTE(Mutiny.StatelessSession session, String classificationName, IEvent<?, ?> withParent, String value, ISystems<?, ?> systems, UUID... identityToken);

    /** Stateless variant of {@link #findEventByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, ISystems, UUID...)}. */
    Uni<IEvent<?, ?>> findEventByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, LocalDateTime, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, LocalDateTime startDate, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, LocalDateTime, LocalDateTime, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, LocalDateTime startDate, LocalDateTime endDate, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findEventByResourceItem(Mutiny.StatelessSession, IResourceItem, String, String, ISystems, UUID...)}. */
    Uni<IEvent<?, ?>> findEventByResourceItem(Mutiny.StatelessSession session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findEventByArrangement(Mutiny.StatelessSession, IArrangement, String, String, ISystems, UUID...)}. */
    Uni<IEvent<?, ?>> findEventByArrangement(Mutiny.StatelessSession session, IArrangement<?, ?> arrangement, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findEventByProduct(Mutiny.StatelessSession, IProduct, String, String, ISystems, UUID...)}. */
    Uni<IEvent<?, ?>> findEventByProduct(Mutiny.StatelessSession session, IProduct<?, ?> product, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findEventsByRules(Mutiny.StatelessSession, IRules, String, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findEventsByRules(Mutiny.StatelessSession session, IRules<?, ?> rules, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #findAll(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
    Uni<List<IEvent<?, ?>>> findAll(Mutiny.StatelessSession session, String eventType, ISystems<?, ?> system, UUID... identityToken);

}
