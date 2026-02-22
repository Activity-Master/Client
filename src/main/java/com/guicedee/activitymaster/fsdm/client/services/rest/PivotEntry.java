package com.guicedee.activitymaster.fsdm.client.services.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * A single pivoted relationship entry with its entity reference, stored value, and timestamp.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class PivotEntry {
    public EntityRef entity;
    public String value;
    public String timestamp;

    public PivotEntry() {
    }
}

