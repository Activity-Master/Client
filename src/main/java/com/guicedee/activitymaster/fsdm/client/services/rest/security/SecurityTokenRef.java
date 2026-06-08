package com.guicedee.activitymaster.fsdm.client.services.rest.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Lightweight reference to a security token — its primary key, its stable {@code securityToken}
 * varchar identity, its structural name and its type (classification name).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class SecurityTokenRef {
    /** The security token primary key (SecurityTokenID). */
    public UUID securityTokenId;
    /** The stable identity varchar (a UUID string) used by grants/links/applicable-token resolution. */
    public String securityToken;
    /** The structural/display name (enterprise-unique), e.g. "Administrators", "Sales Team", "admin". */
    public String name;
    /** The token type — its classification name, e.g. "UserGroup", "User", "Identity". */
    public String type;
}

