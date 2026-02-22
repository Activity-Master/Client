package com.guicedee.activitymaster.fsdm.client.services.rest.arrangements;

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
public class ArrangementCreateDTO {
    @NotNull
    public String type;
    @NotNull
    public String classification;
    @NotNull
    public String typeValue;

    /**
     * Optional classifications to add/update after creation.
     * Key = classification name, Value = classification value.
     * <pre>{@code
     * { "Status": "Active", "Priority": "High", "StartDate": "2025-01-01" }
     * }</pre>
     */
    public Map<String, String> classifications;

    /** Optional involved parties. Key = classification name, Value = store value. */
    public Map<String, String> parties;

    /** Optional resource items. Key = classification name, Value = store value. */
    public Map<String, String> resources;

    /** Optional rules. Key = classification name, Value = store value. */
    public Map<String, String> rules;

    /** Optional products. Key = classification name, Value = store value. */
    public Map<String, String> products;

    /** Optional child arrangement IDs. Key = child arrangement UUID string, Value = relationship value. */
    public Map<String, String> childArrangements;
}

