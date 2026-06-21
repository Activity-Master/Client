package com.guicedee.activitymaster.fsdm.client.services.rest.rules;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.EventActionRequest;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing rule's attributes and relationships.
 * All string keys are <b>names</b> — classification names, rule type names, child rule names.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class RulesUpdateDTO {
    @NotNull
    public UUID rulesId;
    /** Optional new description. */
    public String description;
    /** Classification name → value operations. */
    public RelationshipUpdateEntry classifications;
    /** Classification name → resource item UUID operations. */
    public RelationshipUpdateEntry resources;
    /** Classification name → product UUID operations. */
    public RelationshipUpdateEntry products;
    /** Rule type name → value operations. */
    public RelationshipUpdateEntry ruleTypes;
    /** Child rule name → hierarchy value operations. */
    public RelationshipUpdateEntry children;
    /**
     * Optional event association — when {@code eventId} is set, this update is linked to the event
     * with a change summary (see {@link EventActionRequest}).
     */
    public EventActionRequest event;
}

