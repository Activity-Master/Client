package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Response describing the full set of security tokens an identity expands to — the identity's own
 * token plus every group/folder it is a member of, transitively (the {@code WITH RECURSIVE} climb used
 * by row-level access checks).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ApplicableTokensDTO {
    /** The identity token (securityToken varchar) that was expanded. */
    public String identityToken;
    /** The applicable token primary keys. */
    public List<UUID> applicableIds;
    /** The applicable tokens (id, name, type), when resolvable. */
    public List<SecurityTokenRef> applicable;
}

