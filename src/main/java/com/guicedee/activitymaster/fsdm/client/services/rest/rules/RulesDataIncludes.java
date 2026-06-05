package com.guicedee.activitymaster.fsdm.client.services.rest.rules;

/**
 * Relationship categories that may be hydrated when reading a rule.
 */
public enum RulesDataIncludes {
    /** Classification name → value pairs (purpose, argument, lifecycle, etc.). */
    Classifications,
    /** Classification name → resource item UUIDs (supporting documentation/artefacts). */
    Resources,
    /** Classification name → product UUIDs the rule applies to. */
    Products,
    /** Rule type name → value pairs (structural type links). */
    RuleTypes,
    /** Child rule names → hierarchy values (rule-to-rule composition). */
    Children
}

