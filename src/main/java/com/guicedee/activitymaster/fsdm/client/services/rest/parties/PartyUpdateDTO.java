package com.guicedee.activitymaster.fsdm.client.services.rest.parties;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing involved party's relationships.
 * All string keys are <b>names</b> — classification names, type names, etc.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class PartyUpdateDTO {
    @NotNull
    public UUID partyId;
    /** Classification name → value operations */
    public RelationshipUpdateEntry classifications;
    /** Involved party type classification name → value operations */
    public RelationshipUpdateEntry types;
    /** Name type classification name → value operations */
    public RelationshipUpdateEntry nameTypes;
    /** Identification type classification name → value operations */
    public RelationshipUpdateEntry identificationTypes;
    /** Resource item classification name → value operations */
    public RelationshipUpdateEntry resources;
    /** Product classification name → value operations */
    public RelationshipUpdateEntry products;
    /** Rule classification name → value operations */
    public RelationshipUpdateEntry rules;
    /** Child involved party UUID → value operations */
    public RelationshipUpdateEntry children;
}

