package com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ResourceItemCreateDTO {
    /**
     * Resource item type name (e.g. "ProfilePhoto", "Document").
     */
    @NotNull
    public String type;
    /**
     * The data type / value description for the resource item.
     */
    @NotNull
    public String dataValue;
    /**
     * The data type / value description for the resource item.
     */
    @NotNull
    public UUID key;
    /**
     * Optional binary data (Base64-encoded in JSON).
     */
    public byte[] data;
    /**
     * Optional classifications to add after creation.
     * Key = classification name, Value = classification value.
     */
    public Map<String, String> classifications;
    /**
     * Optional resource item types to add after creation.
     * Key = resource item type name, Value = relationship value.
     */
    public Map<String, String> types;
    /**
     * Optional child resource item IDs to link after creation.
     * Key = classification name (mandatory), Value = child resource item UUID (as string).
     */
    public Map<String, String> children;
}

