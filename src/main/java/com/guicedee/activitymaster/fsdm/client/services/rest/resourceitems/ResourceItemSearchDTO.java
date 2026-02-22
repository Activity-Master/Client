package com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for searching resource items by resource item type and classification.
 * <p>
 * Finds resource items of the given type that have a classification matching the
 * specified name and (optional) value.
 * <p>
 * Supports optional result limiting via {@link #maxResults} and ordering via
 * {@link #sortDirection} and {@link #sortField}. For example, setting
 * {@code maxResults = 1} and {@code sortDirection = DESC} returns only the
 * latest record (by the chosen sort field).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
@Setter
public class ResourceItemSearchDTO {
    /**
     * The resource item type name to filter by.
     */
    @NotNull
    public String resourceItemType;
    /**
     * The classification name to match.
     */
    @NotNull
    public String classificationName;
    /**
     * Optional classification value to match. If null, all items with the classification are returned.
     */
    public String classificationValue;
    /**
     * Which relationship types to include in each result.
     * If empty or null, only the resourceItemId is returned.
     */
    public List<ResourceItemDataIncludes> includes;
    /**
     * Maximum number of results to return. If null or &lt;= 0, all matching results are returned.
     */
    public Integer maxResults;
    /**
     * Sort direction for the results. If null, no explicit ordering is applied.
     * Use {@code ASC} for ascending (oldest first) or {@code DESC} for descending (newest first).
     */
    public SortDirection sortDirection;
    /**
     * Which field to sort results by. Defaults to {@link SearchSortField#EFFECTIVE_FROM_DATE} when
     * a {@link #sortDirection} is specified but no sort field is given.
     */
    public SearchSortField sortField = SearchSortField.WAREHOUSE_CREATED_TIMESTAMP;
}

