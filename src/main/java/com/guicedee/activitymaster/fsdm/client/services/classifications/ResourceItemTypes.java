package com.guicedee.activitymaster.fsdm.client.services.classifications;


import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum ResourceItemTypes
{
	PhysicalDocuments("This is a reference to a physical document", ResourceItemType),
	Documents("An electronic document", ResourceItemType),
	JsonPacket("A JSON packet", ResourceItemType),
	XMLPacket("A XML packet", ResourceItemType),
	Invoices("An invoice", ResourceItemType),
	Statements("A statement", ResourceItemType),
	ElectronicDocuments("A collection of electronic documents", ResourceItemType),
	Icon("An Icon", ResourceItemType),
	Logo("A Logo", ResourceItemType),
	Flag("A Flag", ResourceItemType),
	Banner("A Banner", ResourceItemType),
	Gravatar("A Gravatar", ResourceItemType),
	Screenshot("A Gravatar", ResourceItemType),
	Background("A given background with the applied style", ResourceItemType),
	StyleSheets("A CSS StyleSheet", ResourceItemType),
	
	JavaScriptTemplates("A JavaScript Template", ResourceItemType),
	HtmlTemplate("A HTML 5 Template", ResourceItemType),
	StringTemplate("A String Template", ResourceItemType),
	
	MobileDevice("A physical mobile device", ResourceItemType),
	
	BrowserInformation("Browser Identifying Information", ResourceItemType),
	;
	
	
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;
	
	ResourceItemTypes(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	ResourceItemTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	public String classificationName()
	{
		return name();
	}
	
	public String classificationValue()
	{
		return this.classificationValue;
	}
	
	public String classificationDescription()
	{
		return this.classificationValue;
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
