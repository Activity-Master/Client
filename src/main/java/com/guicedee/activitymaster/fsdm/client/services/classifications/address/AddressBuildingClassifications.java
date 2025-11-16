package com.guicedee.activitymaster.fsdm.client.services.classifications.address;

import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum AddressBuildingClassifications
{
	BuildingAddress("The address of a building", Address),
	BuildingDesk("The Desk Identifier", AddressXClassification),
	BuildingIsle("The isle a desk may be located in", AddressXClassification),
	BuildingFloor("The floor identifier of a building", AddressXClassification),
	BuildingWindow("The building window identifier", AddressXClassification),
	BuildingIdentifer("A building identifier", AddressXClassification),
	BuildingNumber("A building identifier", AddressXClassification),
	BuildingStreet("A building street identifier", AddressXClassification),
	BuildingStreetType("A building street type identifier", AddressXClassification),

	;
	private String classificationValue;
	private EnterpriseClassificationDataConcepts dataConceptValue;

	AddressBuildingClassifications(String classificationValue, EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	AddressBuildingClassifications(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	public String classificationDescription()
	{
		return classificationValue;
	}

	public EnterpriseClassificationDataConcepts concept()
	{
		return dataConceptValue;
	}
	public String toString()
	{
		return name();
	}
}
