package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to grant (or adjust) an access edge from one token to another, specifying the CRUD flags.
 * This is the per-token grant matrix entry used by row-level security checks.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenGrantDTO {
    /** The granting (from) token name. */
    @NotNull
    public String fromName;
    /** The grantee (to) token name. */
    @NotNull
    public String toName;
    /** Whether create access is granted. */
    public boolean create;
    /** Whether update access is granted. */
    public boolean update;
    /** Whether delete access is granted. */
    public boolean delete;
    /** Whether read access is granted. */
    public boolean read;
}

