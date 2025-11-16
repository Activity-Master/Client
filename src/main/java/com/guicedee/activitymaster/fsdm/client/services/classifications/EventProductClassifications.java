package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum EventProductClassifications
{
	ProductEvent("Showed Interest In", EventXProduct),
	ShowedInterestIn("Showed Interest In", EventXProduct),
	Bought("Bought the Product", EventXProduct),
	Sold("Sold the Product", EventXProduct),
	MadeBidFor("Made a Bid For", EventXProduct),
	ChangedBidFor("Changed the Bid For", EventXProduct),
	RemovedBidFor("Removed the Bid For", EventXProduct),
	Cancelled("Didn't Want the Product", EventXProduct),
	DontShowProduct("Asks To Not Show the Product", EventXProduct),
	RemindMeOfTheProduct("Requests To Be Reminded of the Product", EventXProduct),
	ChangedTheCostOf("Changed the Cost Of the Product", EventXProduct),
	AddedTheInterestOf("Added Interest To the Product", EventXProduct),
	ChangedTheInterestOf("Changed the Interest Of the Product", EventXProduct),
	RatedTheProduct("Gave A Rating for the Product", EventXProduct),
	ChangedTheRatingOfTheProduct("Updated the Rating for the Product", EventXProduct),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;
	
	EventProductClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	EventProductClassifications(String classificationValue)
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
