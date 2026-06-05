package com.guicedee.activitymaster.fsdm.client.services.rest.rules;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * Transport shape for a reusable business rule.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class RulesDTO {
    @NotNull
    public UUID rulesId;
    /** Rule set name. */
    public String name;
    /** Rule set description (human-readable rule statement). */
    public String description;
    /** Classification names → stored values. */
    public Map<String, String> classifications;
    /** Classification names → resource item UUIDs. */
    public Map<String, String> resources;
    /** Classification names → product UUIDs the rule applies to. */
    public Map<String, String> products;
    /** Rule type names → stored values. */
    public Map<String, String> ruleTypes;
    /** Child rule names → hierarchy values. */
    public Map<String, String> children;
}

