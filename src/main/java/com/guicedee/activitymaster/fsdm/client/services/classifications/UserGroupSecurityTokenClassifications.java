package com.guicedee.activitymaster.fsdm.client.services.classifications;

public enum UserGroupSecurityTokenClassifications
{
	Administrators("This group defines all the administrators in the system", EnterpriseClassificationDataConcepts.SecurityToken),
	Applications("This group grants access to applications registered within the enterprise", EnterpriseClassificationDataConcepts.SecurityToken),
	Everyone("This groups defines everyone allowed to access the system", EnterpriseClassificationDataConcepts.SecurityToken),
	Everywhere("This groups defines everyone allowed to access the system", EnterpriseClassificationDataConcepts.SecurityToken),

	System("This group defines the systems associated with the enterprise", EnterpriseClassificationDataConcepts.SecurityToken),
	Plugins("This group allows plugins to register under specific security", EnterpriseClassificationDataConcepts.SecurityToken),

	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	UserGroupSecurityTokenClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	UserGroupSecurityTokenClassifications(String classificationValue)
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
