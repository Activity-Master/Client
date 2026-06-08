package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to create a manageable security token (a group/folder or a user/identity token).
 * <p>
 * <strong>Library-managed types are rejected:</strong> {@code System}, {@code Application} and
 * {@code Plugin} tokens (and the Systems/Applications/Plugins folders themselves) are provisioned by
 * the libraries that register them and cannot be created here.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenCreateDTO {
    /** Name of the new token (required, enterprise-unique). */
    @NotNull
    public String name;
    /** Description of the new token. */
    public String description;
    /**
     * Token type — one of the manageable types: {@code UserGroup} (default), {@code User},
     * {@code Identity}, {@code Guests}, {@code Visitors}, {@code Registered}.
     * The library-managed types {@code System}/{@code Application}/{@code Plugin} are rejected.
     */
    public String type;
    /**
     * Optional parent group/folder name to attach this token beneath. Must not be one of the
     * library-managed folders (Systems/Applications/Plugins).
     */
    public String parentName;
}

