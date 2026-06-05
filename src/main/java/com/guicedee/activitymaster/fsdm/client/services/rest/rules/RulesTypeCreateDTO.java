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
public class RulesTypeCreateDTO {
    /** Rule type name (required). Reused if it already exists for the enterprise. */
    @NotNull
    public String name;
    /** Rule type description (defaults to the name when null). */
    public String description;
    /** Optional classifications. Key = classification name, Value = stored value. */
    public Map<String, String> classifications;
    /** Optional supporting resource items. Key = classification name, Value = resource item UUID string. */
    public Map<String, String> resources;
}

