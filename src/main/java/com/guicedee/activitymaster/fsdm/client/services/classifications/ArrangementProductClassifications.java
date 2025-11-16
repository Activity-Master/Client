package com.guicedee.activitymaster.fsdm.client.services.classifications;



import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum ArrangementProductClassifications
{
	ArrangementPurchase("An Arrangement that is a product purchase",ArrangementXProduct),

	PurchaseName("The name of a purchase",ArrangementXProduct),
	PurchaseInvoiceName("The name of a purchase to be displayed on the invoice",ArrangementXProduct),
	PurchaseCost("The cost of a purchase",ArrangementXProduct),
	PurchaseVat("The VAT applied on a purchase",ArrangementXProduct),
	PurchaseTotalCost("The total cost for a purchase",ArrangementXProduct),
	PurchaseInvoiceDate("The date the purchase was invoiced",ArrangementXProduct),
	PurchasePaidDate("The date the purchase was paid",ArrangementXProduct),
	PurchasePromotionCode("The promotion code used on a purchase",ArrangementXProduct),
	PurchaseStatus("The current stage of a purchase",ArrangementXProduct),


	;
	private String classificationValue;
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;

	ArrangementProductClassifications(String classificationValue, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}

	ArrangementProductClassifications(String classificationValue)
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
