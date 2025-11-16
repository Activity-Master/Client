package com.guicedee.activitymaster.fsdm.client.services.classifications;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum DefaultClassifications
{
	HierarchyTypeClassification("Denotes a hierarchy structure type", GlobalClassificationsDataConceptName),
	NoClassification("NoClassification", GlobalClassificationsDataConceptName),
	DefaultClassification("Default Classification", GlobalClassificationsDataConceptName),
	Security("SecurityClassification", GlobalClassificationsDataConceptName),
	;
	private String classificationValue;
	private EnterpriseClassificationDataConcepts dataConceptValue;

	DefaultClassifications(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	DefaultClassifications(String classificationValue, EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	public String classificationDescription()
	{
		return classificationValue;
	}

	public EnterpriseClassificationDataConcepts concept()
	{
		return dataConceptValue;
	}
	
	public String classificationValue()
	{
		return classificationValue;
	}
	
	@Override
	public String toString()
	{
		return name();
	}
}
