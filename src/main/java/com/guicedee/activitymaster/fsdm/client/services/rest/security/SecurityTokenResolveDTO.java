package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Request to expand an identity token into the complete set of security tokens it grants access
 * through (itself plus every group/folder it belongs to, transitively).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenResolveDTO {
    /**
     * The identity token to expand — the {@code securityToken} varchar (a UUID string) of the
     * identity's SecurityToken. Alternatively supply {@link #name} to resolve it by token name.
     */
    public UUID identityToken;
    /** Resolve the identity token by name instead of supplying its {@link #identityToken} varchar. */
    public String name;
}

