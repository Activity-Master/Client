package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request to look up a single security token by {@code name} (preferred) or by its
 * {@code securityToken} varchar identity, optionally hydrating its members / member-of relationships.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenFindDTO {
    /** Structural/display name of the token to find (enterprise-unique). Preferred lookup key. */
    public String name;
    /** Alternatively, the stable {@code securityToken} varchar (a UUID string). */
    public String securityToken;
    /** Which relationship categories to include. If empty/null, only the core fields are returned. */
    public List<SecurityTokenDataIncludes> includes;
}

