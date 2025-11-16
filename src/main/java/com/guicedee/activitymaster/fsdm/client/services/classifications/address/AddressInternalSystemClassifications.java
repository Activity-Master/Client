package com.guicedee.activitymaster.fsdm.client.services.classifications.address;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressInternalSystemClassifications
{
	InternalAddress("The internal address of a connection", Address),
	InternalAddressIPAddress("The internal address IP Address", AddressXClassification),
	InternalAddressHostName("The internal address hostname", AddressXClassification),
	InternalAddressSubnet("The internal address subnet mask", AddressXClassification),
	InternalAddressDns("The internal address DNS", AddressXClassification),
	InternalAddressGateway("The internal address gateway", AddressXClassification),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	AddressInternalSystemClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressInternalSystemClassifications(String classificationValue)
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
