package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum EnterpriseClassifications
{
	LastUpdateDate("The assigned date of the last update", EnterpriseXClassification),
	UpdateClass("The class file for an update", EnterpriseXClassification),
	EnterpriseIdentity("The root UUID of an enterprise", EnterpriseXClassification),
	
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	EnterpriseClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	EnterpriseClassifications(String classificationValue)
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
