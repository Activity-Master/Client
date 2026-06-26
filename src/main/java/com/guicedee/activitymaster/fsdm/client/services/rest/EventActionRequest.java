package com.guicedee.activitymaster.fsdm.client.services.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Optional event association for a create/update REST request.
 * <p>
 * When an {@link #eventId} is supplied on a domain create/update call, the affected entity is
 * linked to that event (via the existing {@code EventX<Domain>} relationship) under an audit
 * <em>action</em> classification, and the human-readable {@link #summary} of the change is recorded
 * as the relationship value. The association persists asynchronously (fire-and-forget) and never
 * affects the primary operation's response.
 *
 * <pre>{@code
 * {
 *   "eventId": "2c7c36bf-ec6d-4b70-a664-47366d3aec0d",
 *   "summary": "Re-uploaded the signed contract after legal review",
 *   "action": "UpdatedTheResourceItem"
 * }
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class EventActionRequest {
    /**
     * The id of the event to associate this action with. When {@code null} the association is skipped.
     */
    public UUID eventId;
    /**
     * A human-readable summary of the change, recorded as the value on the event &rarr; entity link.
     */
    public String summary;
    /**
     * Optional override for the audit action classification name (e.g. {@code "RegisteredTheResourceItem"}).
     * When blank, a sensible per-domain default is used (create vs. update).
     */
    public String action;
}

