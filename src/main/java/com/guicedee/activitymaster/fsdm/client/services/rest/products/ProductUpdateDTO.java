package com.guicedee.activitymaster.fsdm.client.services.rest.products;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.EventActionRequest;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing product's relationships.
 * All string keys are <b>names</b> — classification names, product type names, etc.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ProductUpdateDTO {
    @NotNull
    public UUID productId;
    /** Classification name → value operations */
    public RelationshipUpdateEntry classifications;
    /** Product type name → value operations */
    public RelationshipUpdateEntry types;
    /** Resource item classification name → resource item UUID operations */
    public RelationshipUpdateEntry resources;
    /**
     * Optional event association — when {@code eventId} is set, this update is linked to the event
     * with a change summary (see {@link EventActionRequest}).
     */
    public EventActionRequest event;
}

