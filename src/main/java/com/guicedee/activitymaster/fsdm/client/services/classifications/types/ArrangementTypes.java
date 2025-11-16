package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

public enum ArrangementTypes
{
	
	;
	private String classificationValue;
	
	ArrangementTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	public String classificationValue()
	{
		return name();
	}
	
	public String classificationDescription()
	{
		return classificationValue;
	}
	
	
	public com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		System.out.println("Don't use concepts in arrangement types");
		return null;
	}
	
}
