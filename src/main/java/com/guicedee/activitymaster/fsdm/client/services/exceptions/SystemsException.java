package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class SystemsException
		extends ActivityMasterException
{
	public SystemsException()
	{
	}

	public SystemsException(String message)
	{
		super(message);
	}

	public SystemsException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public SystemsException(Throwable cause)
	{
		super(cause);
	}

	public SystemsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
