package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum EventArrangementClassifications
{
	ArrangementEvents("The arrangement was modified these ways by this event", EventXArrangement),
	Started("Started the Arrangement", EventXArrangement),
	Concluded("Concluded the Arrangement", EventXArrangement),
	AffectedThe("Affected the Arrangement", EventXArrangement),
	RestartedThe("Restarted the Arrangement", EventXArrangement),
	SkippedThe("Skipped the Arrangement", EventXArrangement),
	AlteredRiskValue("Changed the Risk Value of the Arrangement", EventXArrangement),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;
	
	EventArrangementClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	EventArrangementClassifications(String classificationValue)
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
