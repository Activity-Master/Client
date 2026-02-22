package com.guicedee.activitymaster.fsdm.client.services.rest.arrangements;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Request body for the pivot endpoint.
 * Specify lists of names for each relationship type to include.
 * Only non-null, non-empty lists will be queried.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ArrangementPivotRequest {
    @NotNull
    public UUID arrangementId;
    /** Classification descriptions to include (e.g. ["Status", "Priority"]) */
    public List<String> classifications;
    /** Arrangement type names to include (e.g. ["Order", "Invoice"]) */
    public List<String> types;
}

