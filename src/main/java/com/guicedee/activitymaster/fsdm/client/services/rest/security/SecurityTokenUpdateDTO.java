package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to update a manageable security token's description (and optionally rename it).
 * Library-managed tokens/folders cannot be updated through this endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenUpdateDTO {
    /** Name of the token to update (enterprise-unique). */
    @NotNull
    public String name;
    /** Optional new description. */
    public String description;
    /** Optional new name (rename). When blank the name is left unchanged. */
    public String newName;
}

