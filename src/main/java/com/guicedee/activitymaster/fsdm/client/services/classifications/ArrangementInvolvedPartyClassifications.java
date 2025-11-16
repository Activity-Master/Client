package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum ArrangementInvolvedPartyClassifications
{
	InvolvedPartyArrangements("Arrangements from the Involved Party", ArrangementXInvolvedParty),
	PurchasedBy("This arrangement was purchased by", ArrangementXInvolvedParty),
	SoldBy("This arrangement was sold by", ArrangementXInvolvedParty),
	OwnedBy("This arrangement is owned by", ArrangementXInvolvedParty),
	ManagedBy("This arrangement is managed by", ArrangementXInvolvedParty),

	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	ArrangementInvolvedPartyClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	ArrangementInvolvedPartyClassifications(String classificationValue)
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
