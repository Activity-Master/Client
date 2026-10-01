package com.guicedee.activitymaster.fsdm.client.services.dto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** An address grouping; formatted addresses are read projections, never stored values. */
public record PartyAddressDTO(UUID id, String purpose, Map<String, String> components,
                              Map<String, UUID> geographies, Map<String, String> identifiers,
                              Map<String, String> geographyLabels) implements java.io.Serializable
{
    public static final Set<String> PURPOSES = Set.of("Residential", "Postal", "Work", "Delivery", "Billing");
    public static final Set<String> COMPONENTS = Set.of("StreetName", "StreetType", "BoxKind");
    public static final Set<String> LEVELS = Set.of("Country", "Province", "District", "Locality", "PostalArea");
    public static final Set<String> IDENTIFIERS = Set.of("BuildingNumber", "BuildingName", "Unit", "BoxNumber", "PostalCode");

    public PartyAddressDTO {
        if (purpose == null || !PURPOSES.contains(purpose)) throw new IllegalArgumentException("Invalid address purpose");
        components = components == null ? Map.of() : Map.copyOf(components);
        geographies = geographies == null ? Map.of() : Map.copyOf(geographies);
        identifiers = identifiers == null ? Map.of() : Map.copyOf(identifiers);
        geographyLabels = geographyLabels == null ? Map.of() : Map.copyOf(geographyLabels);
        if (!COMPONENTS.containsAll(components.keySet()) || !LEVELS.containsAll(geographies.keySet())
                || !IDENTIFIERS.containsAll(identifiers.keySet()))
            throw new IllegalArgumentException("Invalid address component");
        for (var value : components.values())
            if (value.length() > 150 || !value.equals(value.strip()))
                throw new IllegalArgumentException("Address components must be canonical and at most 150 characters");
        for (var value : identifiers.values())
            if (value.length() > 150 || !value.equals(value.strip()))
                throw new IllegalArgumentException("Address identifiers must be canonical and at most 150 characters");
    }
    public PartyAddressDTO(UUID id, String purpose, Map<String, String> components, Map<String, UUID> geographies, Map<String, String> identifiers) {
        this(id, purpose, components, geographies, identifiers, Map.of());
    }
}
