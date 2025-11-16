package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

public enum OrganicPartyTypes
{
	OrganicCustomerType("Customer"),
	OrganicEmployeeType("Employee"),
	OrganicUserType("User"),
	OrganicAgentType("Agent"),
	OrganicClientType("Client"),
	OrganicUnknownType("Unknown"),

	;
	private String classificationValue;

	OrganicPartyTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	public String classificationValue()
	{
		return classificationValue;
	}
}
