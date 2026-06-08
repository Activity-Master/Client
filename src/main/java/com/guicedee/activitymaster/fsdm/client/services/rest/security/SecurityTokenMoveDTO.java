package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to move a child token from one parent group/folder to another. The old membership edge is
 * temporally closed and a new edge under {@code newParentName} is created. When {@code oldParentName}
 * is omitted, <em>all</em> current parent edges of the child are closed first (an exclusive reparent).
 * Library-managed folders and types are rejected.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenMoveDTO {
    /** The token being moved. */
    @NotNull
    public String childName;
    /** The current parent to detach from, or {@code null} to detach from all current parents. */
    public String oldParentName;
    /** The destination parent group/folder. */
    @NotNull
    public String newParentName;
}

