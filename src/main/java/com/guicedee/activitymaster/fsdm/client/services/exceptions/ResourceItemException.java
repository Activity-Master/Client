package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class ResourceItemException
		extends ActivityMasterException
{
	public ResourceItemException()
	{
	}

	public ResourceItemException(String message)
	{
		super(message);
	}

	public ResourceItemException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ResourceItemException(Throwable cause)
	{
		super(cause);
	}

	public ResourceItemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
