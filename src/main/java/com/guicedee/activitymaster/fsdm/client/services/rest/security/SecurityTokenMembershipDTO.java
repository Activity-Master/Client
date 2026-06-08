package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to add or remove a membership edge between a parent group/folder and a child token.
 * Library-managed folders (Systems/Applications/Plugins) and library-managed child types
 * (System/Application/Plugin) are rejected — those memberships are owned by the registering libraries.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenMembershipDTO {
    /** Parent group/folder name. */
    @NotNull
    public String parentName;
    /** Child token name to add/remove as a member. */
    @NotNull
    public String childName;
}

