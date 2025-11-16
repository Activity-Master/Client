package com.guicedee.activitymaster.fsdm.client.services.classifications.address;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressRemoteSystemClassifications
{
	RemoteAddress("The remote address of a connection", Address),
	RemoteAddressIPAddress("The remote address IP Address", AddressXClassification),
	RemoteAddressHostName("The remote address hostname", AddressXClassification),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	AddressRemoteSystemClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressRemoteSystemClassifications(String classificationValue)
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
