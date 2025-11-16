package com.guicedee.activitymaster.fsdm.client.services.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;
import jakarta.validation.ValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Domain for any given valid phone number on a global scale
 */

public class PhoneNumberDTO
{
    private static final String GenericPhoneNumberRegex = "[\\+]?([0-9]{2})?([0| ])?((\\d{2,3}))[\\)]?[\\-|\\/| ]?(\\d{3})[\\-|\\/| ]?(\\d{4})";
    public static final Pattern GenericPhoneNumberPattern = Pattern.compile(GenericPhoneNumberRegex);
    
    private String countryCode;
    private String spacialCode;
    private String providerCode;
    private String providerSpacialCode;
    private String providerCodeTwice;
    private String areaCode;
    private String identifer;

    private String extension;
    
    private String value;

    public PhoneNumberDTO() {
        // No config required
    }

    public PhoneNumberDTO(String value) {
        assignNumber(value);
    }

    public static PhoneNumberDTO fromString(String value) throws ValidationException
    {
        PhoneNumberDTO dto = new PhoneNumberDTO();
        dto.assignNumber(value);
        return dto;
    }

    public PhoneNumberDTO assignNumber(String value) throws ValidationException {
        this.value = value;
            Matcher matcher = getPattern().matcher(value);
            match(matcher);
        return this;
    }

    /**
     * Overridable for specific types
     *
     * @return
     */
    public Pattern getPattern() {
        return GenericPhoneNumberPattern;
    }

    private void match(Matcher matcher) throws ValidationException {
        if (matcher.find()) {
            countryCode = matcher.group(1);
            spacialCode = matcher.group(2);
            String providerC = matcher.group(3);
            if (providerC == null) {
                providerC = "";
            }
            providerCode = Strings.padStart(providerC, 3, '0')
                    .replace("\\(", "")
                    .replace("\\)", "");
            providerSpacialCode = matcher.group(4);
            areaCode = matcher.group(5);
            identifer = matcher.group(6);

            if (spacialCode == null || spacialCode.trim()
                    .isEmpty()) {
                spacialCode = "0";
            }
            if (providerCodeTwice == null || providerCodeTwice.trim()
                    .isEmpty()) {
                providerCodeTwice = providerCode;
            }
            if (countryCode == null || countryCode.trim()
                    .isEmpty()) {
                countryCode = "27";
            }
        } else {
            throw new ValidationException("The value in the matcher is not a phone number of any identifyable kind.");
        }
    }
    
    public String getCountryCode()
    {
        return countryCode;
    }
    
    public PhoneNumberDTO setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
        return this;
    }
    
    public String getSpacialCode()
    {
        return spacialCode;
    }
    
    public PhoneNumberDTO setSpacialCode(String spacialCode)
    {
        this.spacialCode = spacialCode;
        return this;
    }
    
    public String getProviderCode()
    {
        return providerCode;
    }
    
    public PhoneNumberDTO setProviderCode(String providerCode)
    {
        this.providerCode = providerCode;
        return this;
    }
    
    public String getProviderSpacialCode()
    {
        return providerSpacialCode;
    }
    
    public PhoneNumberDTO setProviderSpacialCode(String providerSpacialCode)
    {
        this.providerSpacialCode = providerSpacialCode;
        return this;
    }
    
    public String getProviderCodeTwice()
    {
        return providerCodeTwice;
    }
    
    public PhoneNumberDTO setProviderCodeTwice(String providerCodeTwice)
    {
        this.providerCodeTwice = providerCodeTwice;
        return this;
    }
    
    public String getAreaCode()
    {
        return areaCode;
    }
    
    public PhoneNumberDTO setAreaCode(String areaCode)
    {
        this.areaCode = areaCode;
        return this;
    }
    
    public String getIdentifer()
    {
        return identifer;
    }
    
    public PhoneNumberDTO setIdentifer(String identifer)
    {
        this.identifer = identifer;
        return this;
    }
    
    public String getExtension()
    {
        return extension;
    }
    
    public PhoneNumberDTO setExtension(String extension)
    {
        this.extension = extension;
        return this;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public PhoneNumberDTO setValue(String value)
    {
        this.value = value;
        return this;
    }
    
    /**
     * Returns the phone number in the format of 270812345678
     *
     * @return
     */
    @JsonValue
    public String getCompleteNumber() {
        if (value != null) {
            return getCountryCode() + getSpacialCode() + getProviderCode().substring(1) + getAreaCode() + getIdentifer();
        }
        return "";
    }

    /**
     * Returns the number as a local number (no 27)
     *
     * @return
     */
    public String toStringLocal() {
        return getCompleteNumber().replaceFirst("27", "0");
    }

    @Override
	public String toString() {
        return getCompleteNumber();
    }


}
