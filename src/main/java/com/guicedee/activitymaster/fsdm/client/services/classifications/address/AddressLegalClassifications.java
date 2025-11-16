package com.guicedee.activitymaster.fsdm.client.services.classifications.address;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressLegalClassifications
{

	LegalAddress("Legal address is used for official purposes such as for serving a notice or for tax reporting. A legal address is used to determine one’s state of legal residence and the state laws to calculate tax. A legal address may include a property’s lot number, block number or district number.", Address),
	LegalDistrictNumber("The district number of an address", AddressXClassification),
	LegalLotNumber("The Lot number of an address", AddressXClassification),
	LegalBlockNumber("The block number of an address", AddressXClassification),

	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	AddressLegalClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressLegalClassifications(String classificationValue)
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
