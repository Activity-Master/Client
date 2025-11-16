package com.guicedee.activitymaster.fsdm.client.services.classifications.address;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressEmailClassifications
{
	EmailAddress("An E-Mail address", Address),
	PersonalEmailAddress("An E-Mail address", Address),
	BusinessEmailAddress("An E-Mail address", Address),
	LegalEmailAddress("An E-Mail address", Address),
	
	EmailAddressHost("The host server of the email address", AddressXClassification),
	EmailAddressDomain("The email address domain", AddressXClassification),
	EmailAddressUser("The user section of the email address", AddressXClassification),
	
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	AddressEmailClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressEmailClassifications(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	public String classificationDescription()
	{
		return classificationValue;
	}

	public com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return dataConceptValue;
	}
	
	public String toString()
	{
		return name();
	}
}
