package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class ActivityMasterException
		extends RuntimeException
{
	public ActivityMasterException()
	{
	}

	public ActivityMasterException(String message)
	{
		super(message);
	}

	public ActivityMasterException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ActivityMasterException(Throwable cause)
	{
		super(cause);
	}

	public ActivityMasterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
