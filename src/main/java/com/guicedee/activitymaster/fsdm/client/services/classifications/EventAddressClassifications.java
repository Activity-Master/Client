package com.guicedee.activitymaster.fsdm.client.services.classifications;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum EventAddressClassifications
{
	AddressEvents("Address Events", EventXAddress),
	
	AddedAddress("Added Address", EventXAddress),
	
	SignedAt("Signed At", EventXAddress),
	OccurredAt("Occurred At", EventXAddress),
	RemoteAddress("With Remote Address", EventXAddress),
	LocalAddress("Local Address Was", EventXAddress),
	PhonedNumber("Called The Number", EventXAddress),
	SentAFax("Sent a Fax To", EventXAddress),
	Emailed("Emailed To", EventXAddress),
	SMSd("Sent a SMS To", EventXAddress),
	MMSd("Sent a MMS To", EventXAddress),
	Posted("Sent By Post To", EventXAddress),
	RegisteredPost("Sent By Registered Post To", EventXAddress),
	
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;
	
	EventAddressClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	EventAddressClassifications(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	public String classificationDescription()
	{
		return classificationValue;
	}
	
	public String classificationValue()
	{
		return name();
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
