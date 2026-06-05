package com.guicedee.activitymaster.fsdm.client.services.rest.classifications;

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
public class ClassificationCreateDTO {
    /** Classification name (required). */
    @NotNull
    public String name;
    /** Classification description. */
    public String description;
    /**
     * Owning ClassificationDataConcept name (must match a known
     * {@code EnterpriseClassificationDataConcepts} value). When null or blank,
     * the default {@code NoClassification} concept is used.
     */
    public String concept;
    /** Sort order within the concept. Defaults to 1 when null. */
    public Integer sequenceNumber;
    /** Optional parent classification name to attach this value beneath in the hierarchy. */
    public String parentName;
    /**
     * Optional child classifications to add after creation.
     * Key = existing child classification name, Value = hierarchy value.
     */
    public Map<String, String> children;
}

