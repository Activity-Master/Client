package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum ArrangementTypeClassifications
{
	ArrangementProductTypes("The arrangement is of a product type",Arrangement),
	ProductPurchase("This arrangement is a product purchase", Arrangement),
	ProductQuote("This arrangement is a product quote", Arrangement),
	ProductBid("This arrangement is a product bid", Arrangement),
	ProductInterest("This arrangement link is interest from the arrangement in the product", Arrangement),
	ProductLead("This arrangement is a product lead", Arrangement),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	ArrangementTypeClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	ArrangementTypeClassifications(String classificationValue)
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
