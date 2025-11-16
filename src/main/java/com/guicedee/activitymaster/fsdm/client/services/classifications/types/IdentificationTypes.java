package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

public enum IdentificationTypes
{
	IdentificationTypeUUID("UUID"),
	IdentificationTypeDriversLicense("Drivers License"),
	IdentificationTypePassportNumber("Passport Number"),
	IdentificationTypeTaxNumber("Tax Number"),
	IdentificationTypeVATNumber("Vat Registration Number"),
	IdentificationTypeRegistrationNumber("Business Registration Number"),
	IdentificationTypeIdentityNumber("Identity Number"),
	IdentificationTypeEmailAddress("Email Address"),
	IdentificationTypeCellPhoneNumber("Cell Phone Number"),
	IdentificationTypeSocialSecurityNumber("SocialSecurityNumber"),
	IdentificationTypeUserName("User Name"),
	//IdentificationTypePassword("User Password"),
	IdentificationTypeSessionID("Session ID"),
	IdentificationTypeSystemID("System ID"),

	IdentificationTypeEnterpriseCreatorRole("Enterprise Creator"),
	IdentificationTypeUnassigned("Unassigned Involved Party"),

	;
	private String classificationValue;

	IdentificationTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	public String classificationValue()
	{
		return name();
	}
	
	public String classificationDescription()
	{
		return classificationValue;
	}
}
