package com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ResourceItemDTO {
    public UUID resourceItemId;
    /** Resource item type names → stored values */
    public Map<String, String> types;
    /** Classification names → stored values */
    public Map<String, String> classifications;
    /** Child resource item IDs → stored values */
    public Map<String, String> children;
}

