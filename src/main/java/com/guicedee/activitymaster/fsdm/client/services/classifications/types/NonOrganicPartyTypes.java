package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

public enum NonOrganicPartyTypes
{
	Organisation("Organisation"),
	Partner("Partner"),
	FinancialOrganisation("FinancialOrganisation"),
	
	;
	private String classificationValue;
	
	NonOrganicPartyTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	public String classificationValue()
	{
		return classificationValue;
	}
}
