package com.guicedee.activitymaster.fsdm.client.services.rest.arrangements;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ArrangementDTO {
    @NotNull
    public UUID arrangementId;
    /** Arrangement Type Name and Stored Relationship Value */
    public Map<String, String> types;
    /** Classification Name and Stored Relationship Value */
    public Map<String, String> classifications;
    /** Involved Party ID and Stored Relationship Value */
    public Map<String, String> parties;
    /** Resource Item ID and Stored Relationship Value */
    public Map<String, String> resources;
    /** Event ID and Stored Relationship Value */
    public Map<String, String> events;
    /** Rules ID and Stored Relationship Value */
    public Map<String, String> rules;
    /** Product ID and Stored Relationship Value */
    public Map<String, String> products;
    /** Rule Type ID and Stored Relationship Value */
    public Map<String, String> ruleTypes;
    /** Child Arrangement ID and Stored Relationship Value */
    public Map<String, String> arrangements;
}

