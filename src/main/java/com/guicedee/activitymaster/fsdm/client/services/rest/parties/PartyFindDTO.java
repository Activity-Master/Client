package com.guicedee.activitymaster.fsdm.client.services.rest.parties;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class PartyFindDTO {
    @NotNull
    public UUID partyId;
    /**
     * Which relationship types to include in the response.
     * If empty or null, only the partyId is returned.
     */
    public List<PartyDataIncludes> includes;
}

