package com.guicedee.activitymaster.fsdm.client.services.rest.parties;

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
public class PartyCreateDTO {
    /**
     * Identification type name (e.g. "Email", "PhoneNumber").
     */
    @NotNull
    public String identificationType;
    /**
     * Identification type value (e.g. "user@example.com", "+1234567890").
     */
    @NotNull
    public String identificationValue;
    /**
     * Whether this involved party is organic (true) or non-organic (false).
     */
    public boolean organic = true;
    /**
     * Optional classifications to add after creation.
     * Key = classification name, Value = classification value.
     */
    public Map<String, String> classifications;
    /**
     * Optional involved party types to add after creation.
     * Key = classification name, Value = store value.
     */
    public Map<String, String> types;
    /**
     * Optional name types to add after creation.
     * Key = classification name, Value = store value.
     */
    public Map<String, String> nameTypes;
    /**
     * Optional resource items. Key = classification name, Value = store value.
     */
    public Map<String, String> resources;
    /**
     * Optional products. Key = classification name, Value = store value.
     */
    public Map<String, String> products;
    /**
     * Optional rules. Key = classification name, Value = store value.
     */
    public Map<String, String> rules;
    /**
     * Optional child involved party IDs. Key = child party UUID string, Value = relationship value.
     */
    public Map<String, String> children;
}

