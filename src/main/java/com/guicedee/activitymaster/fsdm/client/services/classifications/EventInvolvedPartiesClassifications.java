package com.guicedee.activitymaster.fsdm.client.services.classifications;


import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum EventInvolvedPartiesClassifications
{
	InvolvedPartyEvents("Events that Involved Parties can perform", EventXInvolvedParty),
	PerformedBy("Defines the involved party as the one who performed the action", EventXInvolvedParty),
	OnBehalfOf("Defines the involved party as the one who it was done on behalf of. The impersonated user", EventXInvolvedParty),
	For("Defines the involved party who this event was for", EventXInvolvedParty),
	OwnedBy("Defines who owns the event", EventXInvolvedParty),
	
	Created("The Event created the Involved Party", EventXInvolvedParty),
	Added("The Event added the involved party", EventXInvolvedParty),
	Updated("The Event updated the Involved Party", EventXInvolvedParty),
	
	CreatedBy("The Event was created by this user", EventXInvolvedParty),
	UpdatedBy("The Event was updated by this user", EventXInvolvedParty),
	CompletedBy("The Event was completed by this user", EventXInvolvedParty),
	SecurityCredentialsOf("This Event was updated with the security permissions of", EventXInvolvedParty),
	MeantFor("Is Meant For", EventXInvolvedParty),
	Notifies("Creates a Notification For", EventXInvolvedParty),
	
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;
	
	EventInvolvedPartiesClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	EventInvolvedPartiesClassifications(String classificationValue)
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
