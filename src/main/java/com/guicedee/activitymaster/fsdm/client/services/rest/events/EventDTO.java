package com.guicedee.activitymaster.fsdm.client.services.rest.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class EventDTO {
    @NotNull
    public UUID eventId;
    /** Event type names → stored values */
    public Map<String, String> types;
    /** Classification names → stored values */
    public Map<String, String> classifications;
    /** Involved party classification names → involved party IDs */
    public Map<String, String> parties;
    /** Resource item classification names → resource item IDs */
    public Map<String, String> resources;
    /** Product classification names → product IDs */
    public Map<String, String> products;
    /** Rule classification names → rule IDs */
    public Map<String, String> rules;
    /** Arrangement classification names → arrangement IDs */
    public Map<String, String> arrangements;
    /** Child event IDs → stored values */
    public Map<String, String> children;
}

