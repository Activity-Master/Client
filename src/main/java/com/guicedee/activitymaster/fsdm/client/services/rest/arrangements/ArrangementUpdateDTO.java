package com.guicedee.activitymaster.fsdm.client.services.rest.arrangements;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing arrangement's relationships.
 * Each field corresponds to a relationship type and specifies add/update and delete operations.
 * <p>
 * All string keys are <b>names</b> (not IDs) — classification names, type names, product names, etc.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ArrangementUpdateDTO {
    @NotNull
    public UUID arrangementId;
    /** Classification name → value operations */
    public RelationshipUpdateEntry classifications;
    /** Arrangement type name → value operations */
    public RelationshipUpdateEntry types;
    /** Involved party classification name → value operations */
    public RelationshipUpdateEntry parties;
    /** Resource item classification name → value operations */
    public RelationshipUpdateEntry resources;
    /** Rule name → value operations */
    public RelationshipUpdateEntry rules;
    /** Product name → value operations */
    public RelationshipUpdateEntry products;
    /** Rule type name → value operations */
    public RelationshipUpdateEntry ruleTypes;
    /** Child arrangement: Key = classification name (mandatory), Value = child arrangement UUID string operations */
    public RelationshipUpdateEntry childArrangements;
}

