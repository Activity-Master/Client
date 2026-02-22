package com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ResourceItemUpdateDataDTO {
    @NotNull
    public UUID resourceItemId;
    /**
     * The binary data to store (Base64-encoded in JSON).
     */
    @NotNull
    public byte[] data;
}

