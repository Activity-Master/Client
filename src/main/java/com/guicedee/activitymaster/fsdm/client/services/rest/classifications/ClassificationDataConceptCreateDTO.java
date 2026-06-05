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
public class ClassificationDataConceptCreateDTO {
    /**
     * Concept name (required). Must match a known
     * {@code EnterpriseClassificationDataConcepts} value (by enum name or its classification value).
     */
    @NotNull
    public String name;
    /** Concept description. */
    public String description;
    /**
     * Optional resource items to attach after creation.
     * Key = value classification name, Value = resource item UUID string.
     */
    public Map<String, String> resources;
}

