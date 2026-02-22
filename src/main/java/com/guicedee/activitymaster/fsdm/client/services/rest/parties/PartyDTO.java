package com.guicedee.activitymaster.fsdm.client.services.rest.parties;

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
public class PartyDTO {
    @NotNull
    public UUID partyId;
    /** Involved party type classification names → stored values */
    public Map<String, String> types;
    /** Classification names → stored values */
    public Map<String, String> classifications;
    /** Name type classification names → stored values */
    public Map<String, String> nameTypes;
    /** Identification type classification names → stored values */
    public Map<String, String> identificationTypes;
    /** Resource item classification names → resource item IDs */
    public Map<String, String> resources;
    /** Product classification names → product IDs */
    public Map<String, String> products;
    /** Rule classification names → rule IDs */
    public Map<String, String> rules;
    /** Address classification names → address IDs */
    public Map<String, String> addresses;
    /** Child involved party IDs → stored values */
    public Map<String, String> children;
}

