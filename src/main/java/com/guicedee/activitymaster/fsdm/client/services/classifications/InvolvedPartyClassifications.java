package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum InvolvedPartyClassifications
{
	SecurityPassword("Defines a password if any is required for a security token", InvolvedPartyXClassification),
	SecurityPasswordSalt("Defines the salt used for the password", InvolvedPartyXClassification),

	Languages("Defines Languages as their ISO counterpart", GlobalClassificationsDataConceptName),
	ISO639_1("ISO 639 is a set of international standards that lists short codes for language names.", InvolvedPartyXClassification),
	ISO639_2("ISO 639 is a set of international standards that lists short codes for language names.", InvolvedPartyXClassification),
	ISO6392EnglishName("The english name of a language.", InvolvedPartyXClassification),
	ISO6392FrenchName("ISO 639 is a set of international standards that lists short codes for language names.", InvolvedPartyXClassification),
	ISO6392GermanName("ISO 639 is a set of international standards that lists short codes for language names.", InvolvedPartyXClassification),

	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	InvolvedPartyClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	InvolvedPartyClassifications(String classificationValue)
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
