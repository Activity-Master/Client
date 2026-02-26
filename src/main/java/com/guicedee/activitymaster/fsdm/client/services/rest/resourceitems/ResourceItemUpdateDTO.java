package com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ResourceItemUpdateDTO {
    @NotNull
    public UUID resourceItemId;
    /** Classification name → value operations */
    public RelationshipUpdateEntry classifications;
    /** Resource item type name → value operations */
    public RelationshipUpdateEntry types;
    /** Child resource item: Key = classification name (mandatory), Value = child UUID string operations */
    public RelationshipUpdateEntry children;
}

