package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum EventTypeClassifications
{

	TypeOfEvents("Event Also has Types", EventXEventType),
	HasTheType("Has The Type", EventXEventType),
	CanBeIdentifiedBy("Can Be Identified the Type", EventXEventType),
	SubType("Has the Subtype", EventXEventType),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	EventTypeClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	EventTypeClassifications(String classificationValue)
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

	public String classificationValue()
	{
		return name();
	}
	
	public String toString()
	{
		return name();
	}
}
