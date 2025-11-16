package com.guicedee.activitymaster.fsdm.client.services.classifications.address;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressWebClassifications
{

	WebAddress("An HTTP or HTTPS URL Address", Address),
	WebAddressProtocol("The Protocol used for a web address", AddressXClassification),
	WebAddressPort("The port used to connect to the web address", AddressXClassification),
	WebAddressSubDomain("The Sub domain portion of an address", AddressXClassification),
	WebAddressDomain("The domain portion of an address", AddressXClassification),
	WebAddressUrl("The complete URL of a given address", AddressXClassification),
	WebAddressQueryParameters("The query parameters of an address", AddressXClassification),
	WebAddressSite("The site portion of a web address", AddressXClassification),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	AddressWebClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressWebClassifications(String classificationValue)
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
