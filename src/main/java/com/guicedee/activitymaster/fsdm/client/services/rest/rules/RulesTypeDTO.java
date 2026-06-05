package com.guicedee.activitymaster.fsdm.client.services.rest.rules;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * Transport shape for a rule type (the structural/implementation classification of a rule).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class RulesTypeDTO {
    @NotNull
    public UUID rulesTypeId;
    /** Rule type name (e.g. {@code Single Rule}, {@code Range Rule}). */
    public String name;
    /** Rule type description. */
    public String description;
    /** Classification names → stored values. */
    public Map<String, String> classifications;
    /** Classification names → resource item UUIDs. */
    public Map<String, String> resources;
}

