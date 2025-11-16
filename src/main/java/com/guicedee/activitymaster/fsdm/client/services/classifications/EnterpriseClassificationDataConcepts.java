package com.guicedee.activitymaster.fsdm.client.services.classifications;

public enum EnterpriseClassificationDataConcepts
{
	NoClassificationDataConceptName("NoClassification"),
	GlobalClassificationsDataConceptName("GlobalClassifications"),

	ActiveFlag,
	ActiveFlagXClassification,
	
	Address,
	AddressXClassification,
	AddressXGeography,
	AddressXInvolvedParty,
	AddressXResourceItem,
	
	Arrangement,
	ArrangementType,
	ArrangementXArrangement,
	ArrangementXArrangementType,
	ArrangementXClassification,
	ArrangementXInvolvedParty,
	ArrangementXProduct,
	ArrangementXResourceItem,
	ArrangementXRules,
	ArrangementXRulesTypes,
	
	
	Classification,
	ClassificationDataConcept,
	ClassificationDataConceptXClassification,
	ClassificationDataConceptXResourceItem,

	ClassificationXClassification,

	ClassificationXResourceItem,
	
	
	
	Enterprise,
	EnterpriseXClassification,
	
	
	Event,
	EventType,
	EventXAddress,
	EventXArrangement,
	EventXClassification,
	EventXEventType,
	EventXGeography,
	EventXInvolvedParty,
	EventXProduct,
	EventXResourceItem,
	EventXRules,
	
	
	Geography,
	GeographyXClassification,
	GeographyXGeography,
	GeographyXResourceItem,
	
	
	
	
	InvolvedParty,
	InvolvedPartyIdentificationType,
	InvolvedPartyNameType,
	InvolvedPartyNonOrganic,
	InvolvedPartyOrganic,
	InvolvedPartyOrganicType,
	InvolvedPartyType,
	InvolvedPartyXAddress,
	InvolvedPartyXClassification,
	InvolvedPartyXInvolvedParty,
	InvolvedPartyXInvolvedPartyIdentificationType,
	InvolvedPartyXInvolvedPartyNameType,
	InvolvedPartyXInvolvedPartyType,
	InvolvedPartyXProduct,
	InvolvedPartyXResourceItem,
	
	
	Product,
	ProductType,
	ProductXClassification,
	ProductXProduct,
	ProductXProductType,
	ProductXResourceItem,
	
	
	ResourceItem,
	ResourceItemData,
	ResourceItemDataXClassification,
	ResourceItemType,
	ResourceItemXClassification,
	ResourceItemXResourceItem,
	ResourceItemXResourceItemType,
	
	
	Rules,
	RulesXProduct,
	RulesXResourceItem,
	RulesType,
	RulesTypeXClassification,
	
	
	SecurityToken,
	SecurityTokenXClassification,
	SecurityTokenXSecurityToken,
	
	Systems,
	SystemXClassification,
	
	
	YesNo,
	YesNoXClassification,
	
	InvolvedPartyXRules;

	private String classificationValue;

	EnterpriseClassificationDataConcepts()
	{
		this.classificationValue = name();
	}

	EnterpriseClassificationDataConcepts(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	public static EnterpriseClassificationDataConcepts fromClassName(Class clazz)
	{
		return EnterpriseClassificationDataConcepts.valueOf(clazz.getSimpleName());
	}
	
	
	public String classificationValue()
	{
		return classificationValue;
	}
	
	public String toString()
	{
		return name();
	}
}
