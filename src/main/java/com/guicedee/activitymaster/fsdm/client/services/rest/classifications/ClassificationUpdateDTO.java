package com.guicedee.activitymaster.fsdm.client.services.rest.classifications;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for updating an existing classification's attributes and hierarchy relationships.
 * All string keys are <b>names</b> — child classification names.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ClassificationUpdateDTO {
    @NotNull
    public UUID classificationId;
    /** Optional new description. */
    public String description;
    /** Optional new sequence number. */
    public Integer sequenceNumber;
    /** Child classification name → hierarchy value operations. */
    public RelationshipUpdateEntry children;
}

