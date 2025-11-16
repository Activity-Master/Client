package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum ResourceItemClassifications
{
	FileResourceItemClassifications("Resource items relating to a file",ResourceItemXClassification),
	EventDefaultResourceItemClassifications("Default classifications relating to an event",ResourceItemXClassification),

	AddedANewDevice("The Involved Party registered a new device", ResourceItemXClassification),
	HadNewConnectionDetails("The Involved Party registered a set of details identifying their browser connection", ResourceItemXClassification),
	Description("The description of the file resource item", ResourceItemXClassification),
	Extension("The extension of the file resource item", ResourceItemXClassification),
	FileName("The filename of the resource item", ResourceItemXClassification),
	Size("The File Size in Bytes", ResourceItemXClassification),
	Icon("Any custom icon for the file", ResourceItemXClassification),
	FileLocation("A set location of somewhere for this file", ResourceItemXClassification),
	UUID("A unique UUID for a resource item", ResourceItemXClassification),

	Added("Added a new",ResourceItemXClassification),
	Removed("Removed the ",ResourceItemXClassification),
	Updated("Updated the ",ResourceItemXClassification),
	MovedTo("Moved the item to",ResourceItemXClassification),
	
	Hardware("Specifies a resource item as a Hardware resource",ResourceItemXClassification),
	Scanner("Specifies a resource item as a Scanner resource",ResourceItemXClassification),
	Printer("Specifies a resource item as a Printer resource",ResourceItemXClassification),
	Computer("Specifies a resource item as a Computer resource",ResourceItemXClassification),
	Phone("Specifies a resource item as a Phone resource",ResourceItemXClassification),
	Desktop("Specifies a resource item as a Computer/Desktop resource",ResourceItemXClassification),
	Laptop("Specifies a resource item as a Computer/Laptop resource",ResourceItemXClassification),
	
	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	ResourceItemClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	ResourceItemClassifications(String classificationValue)
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
