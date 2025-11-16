package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum SecurityTokenClassifications
{
	Identity("Defines an identity classification relationship", SecurityTokenXSecurityToken),
	Guests("Defines an item as a user with no security applied", SecurityTokenXSecurityToken),
	Visitors("Defines an item as a user with no previous identification", SecurityTokenXSecurityToken),
	Registered("Defines an item as a user who has registered with guest access", SecurityTokenXSecurityToken),


	UserGroup("Defines an item as a user group with security applied", SecurityTokenXSecurityToken),
	User("Defines an item as a user with security applied", SecurityTokenXSecurityToken),
	Application("Defines an item as an application with security applied", SecurityTokenXSecurityToken),
	Plugin("Defines an item as a plugin with security applied", SecurityTokenXSecurityToken),


	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	SecurityTokenClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	SecurityTokenClassifications(String classificationValue)
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
