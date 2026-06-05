package com.guicedee.activitymaster.fsdm.client.services.rest.classifications;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * Transport shape for a single classification value.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ClassificationDTO {
    @NotNull
    public UUID classificationId;
    /** Short display name of the classification value. */
    public String name;
    /** Human-readable explanation of the classification value. */
    public String description;
    /** Sort order within the concept. */
    public Integer sequenceNumber;
    /** Name of the owning ClassificationDataConcept. */
    public String concept;
    /** Child classification names → hierarchy values. */
    public Map<String, String> children;
}

