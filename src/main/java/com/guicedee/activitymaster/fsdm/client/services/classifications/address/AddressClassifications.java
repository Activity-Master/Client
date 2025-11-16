package com.guicedee.activitymaster.fsdm.client.services.classifications.address;

import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;


public enum AddressClassifications
{

	Address("An Address", EnterpriseClassificationDataConcepts.Address),
	LocationAddress("An address that is a physical location", EnterpriseClassificationDataConcepts.Address),
	ContactAddress("An address for contacting an involved party", EnterpriseClassificationDataConcepts.Address),
	PostalAddress("An address for posting to an involved party", EnterpriseClassificationDataConcepts.Address),
	CallAddress("An address for making a call to an involved party", EnterpriseClassificationDataConcepts.Address),
	InternetAddress("An address that is an IP for an involved party", EnterpriseClassificationDataConcepts.Address),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	AddressClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressClassifications(String classificationValue)
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
