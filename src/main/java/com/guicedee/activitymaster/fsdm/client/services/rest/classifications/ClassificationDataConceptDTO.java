package com.guicedee.activitymaster.fsdm.client.services.rest.classifications;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * Transport shape for a classification data concept (the reusable bucket/scheme).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ClassificationDataConceptDTO {
    @NotNull
    public UUID conceptId;
    /** Stable, code-safe concept name (e.g. {@code IndustryClassifications}). */
    public String name;
    /** Friendly explanation of the question, scheme or bucket. */
    public String description;
    /** Classification value names → descriptions belonging to this concept. */
    public Map<String, String> classifications;
    /** Value classification names → resource item IDs attached to this concept. */
    public Map<String, String> resources;
}

