package com.guicedee.activitymaster.fsdm.client.services.exceptions;

import java.io.Serial;

public class ProductException
		extends ActivityMasterException
{
	@Serial
	private static final long serialVersionUID = -6675371806272654345L;
	
	public ProductException()
	{
	}
	
	public ProductException(String message)
	{
		super(message);
	}
	
	public ProductException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public ProductException(Throwable cause)
	{
		super(cause);
	}
	
	public ProductException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
