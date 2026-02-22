package com.guicedee.activitymaster.fsdm.client.services.rest.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing event's relationships.
 * All string keys are <b>names</b> — classification names, type names, etc.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class EventUpdateDTO {
    @NotNull
    public UUID eventId;
    /** Classification name → value operations */
    public RelationshipUpdateEntry classifications;
    /** Event type name → value operations */
    public RelationshipUpdateEntry types;
    /** Involved party classification name → value operations */
    public RelationshipUpdateEntry parties;
    /** Resource item classification name → value operations */
    public RelationshipUpdateEntry resources;
    /** Product classification name → value operations */
    public RelationshipUpdateEntry products;
    /** Rule classification name → value operations */
    public RelationshipUpdateEntry rules;
    /** Arrangement classification name → value operations */
    public RelationshipUpdateEntry arrangements;
    /** Child event UUID → value operations */
    public RelationshipUpdateEntry children;
}

