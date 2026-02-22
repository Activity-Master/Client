package com.guicedee.activitymaster.fsdm.client.services.rest.parties;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for searching involved parties by identification type name and value.
 * <p>
 * Finds involved parties that have an identification type matching the specified
 * type name and (optional) value. For example, searching by identification type
 * "Email" with value "user@example.com".
 * <p>
 * Supports optional result limiting via {@link #maxResults} and optional includes
 * to control which relationship types are returned per result.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class PartySearchByIdentificationDTO {
    /**
     * The identification type name to match (e.g. "Email", "PhoneNumber", "IdentificationTypeUUID").
     */
    @NotNull
    public String identificationType;
    /**
     * Optional identification value to match. If null, all parties with the identification type are returned.
     */
    public String identificationValue;
    /**
     * Which relationship types to include in each result.
     * If empty or null, only the partyId is returned.
     */
    public List<PartyDataIncludes> includes;
    /**
     * Maximum number of results to return. If null or &lt;= 0, all matching results are returned.
     */
    public Integer maxResults;
}

