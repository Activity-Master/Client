package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum ProductClassifications
{
	Products("Product Details", ProductXClassification),
	ProductGroup("The group of a product", ProductXClassification),
	ProductTypeName("The type of a product",ProductXClassification),
	ProductPremiumType("If this product is a premium product or not",ProductXClassification),
	ProductBaseCost("The base cost of this product excluding VAT",ProductXClassification),
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	ProductClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	ProductClassifications(String classificationValue)
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
