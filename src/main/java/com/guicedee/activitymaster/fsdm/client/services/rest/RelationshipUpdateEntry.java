package com.guicedee.activitymaster.fsdm.client.services.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Specifies add/update and delete operations for a single relationship type.
 * <p>
 * {@code addOrUpdate} — Key/value pairs to add or update (key is the name, value is the stored value).
 * {@code delete} — List of names to expire (soft-delete).
 *
 * <pre>{@code
 * {
 *   "addOrUpdate": { "Status": "Active", "Priority": "High" },
 *   "delete": ["OldStatus", "Deprecated"]
 * }
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class RelationshipUpdateEntry {
    /**
     * Key/value pairs to add or update.
     * Key = name (classification name, type name, etc.). Value = relationship value.
     */
    public Map<String, String> addOrUpdate;
    /**
     * Names to expire (soft-delete).
     */
    public List<String> delete;
}

