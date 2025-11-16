package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class SecurityAccessException
		extends ActivityMasterException
{
	public SecurityAccessException()
	{
	}

	public SecurityAccessException(String message)
	{
		super(message);
	}

	public SecurityAccessException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public SecurityAccessException(Throwable cause)
	{
		super(cause);
	}

	public SecurityAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
