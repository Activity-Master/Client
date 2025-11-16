package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

public enum NameTypes
{
	FirstNameType("First Name"),
	FullNameType("Full Name"),
	PreferredNameType("Preferred Name"),
	BirthNameType("Birth Name"),
	LegalNameType("Legal Name"),
	CommonNameType("Common Name"),
	SalutationType("Salutation Name"),
	MiddleNameType("Middle Name"),
	InitialsType("Initials"),
	SurnameType("Surname"),
	QualificationType("Qualification"),
	SuffixType("Suffix");
	
	private String classificationValue;
	
	NameTypes(String classificationValue)
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
}
