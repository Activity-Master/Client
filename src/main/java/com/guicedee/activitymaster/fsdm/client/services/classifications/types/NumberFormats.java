package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public enum NumberFormats
{
	DoubleDigits(",00"),
	;
	private String format;
	private DecimalFormat decimalFormat;

	NumberFormats(String format)
	{
		this.format = format;
		decimalFormat = new DecimalFormat(this.format);
	}

	public NumberFormat formatter()
	{
		return decimalFormat;
	}

	public String getFormat()
	{
		return format;
	}
}
