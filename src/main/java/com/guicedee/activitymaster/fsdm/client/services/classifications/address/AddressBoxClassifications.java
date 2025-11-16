package com.guicedee.activitymaster.fsdm.client.services.classifications.address;

import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressBoxClassifications
{

	BoxAddress("A Post Box Address", Address),
	BoxNumber("The number of the box", AddressXClassification),
	BoxIdentifier("Identifier of the box, E.g. PO BOX", AddressXClassification),
	BoxCity("The city of the postal box", AddressXClassification),
	BoxPostalCode("The postal code of the post box", AddressXClassification),
	;
	private String classificationValue;
	private EnterpriseClassificationDataConcepts dataConceptValue;

	AddressBoxClassifications(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	AddressBoxClassifications(String classificationValue, EnterpriseClassificationDataConcepts dataConceptValue)
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
	
	public String toString()
	{
		return name();
	}
}
