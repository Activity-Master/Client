package com.guicedee.activitymaster.fsdm.client.services.rest.products;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.EventActionRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ProductCreateDTO {
    /** Product name (required). */
    @NotNull
    public String name;
    /** Product description. */
    public String description;
    /** Product code (short catalogue code). */
    public String code;
    /**
     * Product types to create/associate.
     * Key = product type name, Value = relationship value.
     * At least one entry is required — the first entry is used as the primary product type during creation.
     */
    @NotNull
    public Map<String, String> types;
    /**
     * Optional classifications to add after creation.
     * Key = classification name, Value = classification value.
     */
    public Map<String, String> classifications;
    /**
     * Optional resource items. Key = classification name, Value = resource item UUID string.
     */
    public Map<String, String> resources;
    /**
     * Optional event association — when {@code eventId} is set, this create is linked to the event
     * with a change summary (see {@link EventActionRequest}).
     */
    public EventActionRequest event;
}

