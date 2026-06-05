package com.guicedee.activitymaster.fsdm.client.services.rest.rules;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing rule type's attributes and relationships.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class RulesTypeUpdateDTO {
    @NotNull
    public UUID rulesTypeId;
    /** Optional new description. */
    public String description;
    /** Classification name → value operations. */
    public RelationshipUpdateEntry classifications;
    /** Classification name → resource item UUID operations. */
    public RelationshipUpdateEntry resources;
}

