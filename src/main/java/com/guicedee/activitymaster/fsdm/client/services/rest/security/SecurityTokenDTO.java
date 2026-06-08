package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Full transport shape for a single security token, optionally hydrated with its direct members
 * (children) and the groups/folders it belongs to (parents).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenDTO {
    /** The security token primary key (SecurityTokenID). */
    public UUID securityTokenId;
    /** The stable identity varchar (a UUID string). */
    public String securityToken;
    /** Structural/display name. */
    public String name;
    /** Human-readable description. */
    public String description;
    /** Token type — its classification name (e.g. "UserGroup", "User", "Identity"). */
    public String type;
    /**
     * {@code true} when this token is a library-managed structure (the Systems/Applications/Plugins
     * folders, or a System/Application/Plugin-typed token) and therefore cannot be mutated through this
     * endpoint.
     */
    public Boolean managed;
    /** Direct members (children) — populated only when {@code Members} is requested. */
    public List<SecurityTokenRef> members;
    /** Groups/folders this token belongs to (parents) — populated only when {@code MemberOf} is requested. */
    public List<SecurityTokenRef> memberOf;
}

