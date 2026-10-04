package com.guicedee.activitymaster.fsdm.client.services.dto;

import java.util.Set;
import java.util.UUID;

/** An owned telephone address; no owner identity is accepted from the caller. */
public record PartyPhoneDTO(UUID id, String type, String number, String extension) implements java.io.Serializable {
    public static final Set<String> TYPES = Set.of("HomeTelephoneNumber", "BusinessTelephoneNumber", "LegalTelephoneNumber",
        "HomeCellNumber", "BusinessCellNumber", "LegalCellNumber", "HomeFaxNumber", "BusinessFaxNumber", "LegalFaxNumber",
        "HomePagerNumber", "BusinessPagerNumber", "LegalPagerNumber");
    public static final String NUMBER_PATTERN = "(?=.{1,64}$)(?=(?:[^0-9]*[0-9]){7,15}[^0-9]*$)\\+?[0-9]+(?:[ .-]?[0-9]+| ?\\([0-9]+\\))*";
    public PartyPhoneDTO {
        if (!TYPES.contains(type == null ? "" : type)) throw new IllegalArgumentException("Invalid telephone address type");
        if (number == null || !number.matches(NUMBER_PATTERN)) throw new IllegalArgumentException("Invalid telephone number");
        extension = extension == null ? "" : extension;
        if (!extension.matches("[0-9]{0,10}")) throw new IllegalArgumentException("Invalid telephone extension");
    }
}
