package com.guicedee.activitymaster.fsdm.client.services.rest.classifications;

/**
 * Relationship categories that may be hydrated when reading a classification data concept.
 */
public enum ClassificationDataConceptDataIncludes {
    /** Classification values that belong to this concept. */
    Classifications,
    /** Resource items (supporting documentation) attached to this concept. */
    Resources
}

