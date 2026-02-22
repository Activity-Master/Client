package com.guicedee.activitymaster.fsdm.client.services.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * A reference to a named entity (classification, arrangement type, etc.)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class EntityRef {
    public String id;
    public String name;

    public EntityRef() {
    }

    public EntityRef(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

