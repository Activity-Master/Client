package com.guicedee.activitymaster.fsdm.client.services.rest.rules;

/**
 * Relationship categories that may be hydrated when reading a rule type.
 */
public enum RulesTypeDataIncludes {
    /** Classification name → value pairs classifying the rule type. */
    Classifications,
    /** Classification name → resource item UUIDs (specifications, examples, artefacts). */
    Resources
}

