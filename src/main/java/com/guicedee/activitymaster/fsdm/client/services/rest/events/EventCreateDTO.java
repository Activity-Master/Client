package com.guicedee.activitymaster.fsdm.client.services.rest.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class EventCreateDTO {
    /**
     * Event types to create/associate.
     * Key = event type name (e.g. "Login", "PackingSession"), Value = relationship value.
     * At least one entry is required — the first entry is used as the primary event type during creation.
     */
    @NotNull
    public Map<String, String> types;
    /**
     * Optional classifications to add after creation.
     * Key = classification name, Value = classification value.
     */
    public Map<String, String> classifications;
    /**
     * Optional involved parties. Key = classification name, Value = store value.
     */
    public Map<String, String> parties;
    /**
     * Optional resource items. Key = classification name, Value = store value.
     */
    public Map<String, String> resources;
    /**
     * Optional products. Key = classification name, Value = store value.
     */
    public Map<String, String> products;
    /**
     * Optional rules. Key = classification name, Value = store value.
     */
    public Map<String, String> rules;
    /**
     * Optional arrangements. Key = classification name, Value = store value.
     */
    public Map<String, String> arrangements;
    /**
     * Optional child event IDs. Key = child event UUID string, Value = relationship value.
     */
    public Map<String, String> children;
}


