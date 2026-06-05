package com.guicedee.activitymaster.fsdm.client.services.rest.products;

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
public class ProductDTO {
    @NotNull
    public UUID productId;
    /** Product name */
    public String name;
    /** Product description */
    public String description;
    /** Product code */
    public String code;
    /** Product type names → stored values */
    public Map<String, String> types;
    /** Classification names → stored values */
    public Map<String, String> classifications;
    /** Resource item classification names → resource item IDs */
    public Map<String, String> resources;
}

