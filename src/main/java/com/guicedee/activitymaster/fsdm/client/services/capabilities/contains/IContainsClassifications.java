package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;


import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;

public interface IContainsClassifications<J extends IContainsClassifications<J>>
{
	IClassification<?,?> getClassificationID();
	
	J setClassificationID(IClassification<?,?> classificationID);
	
}
