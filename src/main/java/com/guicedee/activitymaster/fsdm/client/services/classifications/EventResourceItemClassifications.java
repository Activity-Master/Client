package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum EventResourceItemClassifications
{
	ResourceItemEvent("Event performed on the resource", EventXResourceItem),
	AddedResourceItem("Added the Resource", EventXResourceItem),
	ChangedTheResourceItem("Changed the Resource", EventXResourceItem),
	UpdatedTheResourceItem("Updated the Resource", EventXResourceItem),
	RemovedTheResourceItem("Removed the Resource", EventXResourceItem),
	RegisteredTheResourceItem("Registered the Resource", EventXResourceItem),
	RemovedTheResourceItemRegistration("Removed a Registration for the Resource", EventXResourceItem),
	LodgedTheResourceItemRegistration("Lodged the Resource", EventXResourceItem),
	DeliveredTheResourceItemRegistration("Delivered the Resource", EventXResourceItem),
	DestroyedTheResourceItemRegistration("Destroyed the Resource", EventXResourceItem),
	
	
	JSONCallRequest("With a JSON Request Of", EventXResourceItem),
	JSONCallResponse("The JSON Response Was", EventXResourceItem),
	WebServiceCallRequest("With a WebService Request To", EventXResourceItem),
	WebServiceCallResponse("The WebService Response Was", EventXResourceItem),
	
	HttpCallRequest("With a Http Request Of", EventXResourceItem),
	HttpCallResponse("The Http Response Was", EventXResourceItem),
	HttpSession("The HttpSession details", EventXResourceItem),
	HttpSessionProperties("The Session Properties was", EventXResourceItem),
	
	UserAgent("The User Agent details were", EventXResourceItem),
	
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;
	
	EventResourceItemClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	EventResourceItemClassifications(String classificationValue)
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
