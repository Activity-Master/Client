package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

public enum IPTypes
{
	TypeIndividual("Individual"),
	TypeOrganisation("Organisation"),
	TypeSystem("System"),
	TypeDevice("Device"),
	TypeApplication("Application"),
	TypeAbstraction("Abstraction"),
	TypeUnknown("Unknown"),
	
	
	;
	private String classificationValue;
	
	IPTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	
	public String classificationValue()
	{
		return classificationValue;
	}
}
