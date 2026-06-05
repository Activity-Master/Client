package com.guicedee.activitymaster.fsdm.client.services.rest.rules;

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
public class RulesCreateDTO {
    /** Rule set name (required). */
    @NotNull
    public String name;
    /** Rule set description (human-readable rule statement). */
    public String description;
    /**
     * Optional classifications (purpose, argument, lifecycle, reference type, etc.).
     * Key = classification name, Value = stored value.
     */
    public Map<String, String> classifications;
    /**
     * Optional supporting resource items. Key = classification name, Value = resource item UUID string.
     */
    public Map<String, String> resources;
    /**
     * Optional products the rule applies to. Key = classification name, Value = product UUID string.
     */
    public Map<String, String> products;
    /**
     * Optional rule type links (structural type). Key = rule type name, Value = stored value.
     */
    public Map<String, String> ruleTypes;
    /**
     * Optional child rules (rule-to-rule composition). Key = existing child rule name, Value = hierarchy value.
     */
    public Map<String, String> children;
}

