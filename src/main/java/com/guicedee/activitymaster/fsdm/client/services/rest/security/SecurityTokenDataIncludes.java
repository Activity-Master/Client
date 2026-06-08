package com.guicedee.activitymaster.fsdm.client.services.rest.security;

/**
 * Relationship categories that may be hydrated when reading a security token.
 */
public enum SecurityTokenDataIncludes {
    /** Direct members (child tokens) of this group/folder. */
    Members,
    /** Groups/folders this token belongs to (direct parents). */
    MemberOf
}

