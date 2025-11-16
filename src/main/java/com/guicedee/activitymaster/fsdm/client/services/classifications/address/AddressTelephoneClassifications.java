package com.guicedee.activitymaster.fsdm.client.services.classifications.address;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressTelephoneClassifications
{
	TelephoneNumber("Any given home telephone number", Address),
	
	HomeTelephoneNumber("Any given home telephone number", Address),
	BusinessTelephoneNumber("Any given business telephone number", Address),
	LegalTelephoneNumber("Any given legal telephone number", Address),
	
	HomeCellNumber("Any given home telephone number", Address),
	BusinessCellNumber("Any given business telephone number", Address),
	LegalCellNumber("Any given legal telephone number", Address),
	
	HomeFaxNumber("Any given home telephone number", Address),
	BusinessFaxNumber("Any given business telephone number", Address),
	LegalFaxNumber("Any given legal telephone number", Address),
	
	HomePagerNumber("Any given home telephone number", Address),
	BusinessPagerNumber("Any given business telephone number", Address),
	LegalPagerNumber("Any given legal telephone number", Address),
	
	
	TelephoneCountryCode("The country code for the telephone number", AddressXClassification),
	TelephoneExtensionNumber("Telephone Number Extension", AddressXClassification),
	TelephoneAreaCode("The area code for the address", AddressXClassification),

	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	AddressTelephoneClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressTelephoneClassifications(String classificationValue)
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
