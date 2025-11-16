package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class TimeException
		extends ActivityMasterException
{
	public TimeException()
	{
	}

	public TimeException(String message)
	{
		super(message);
	}

	public TimeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public TimeException(Throwable cause)
	{
		super(cause);
	}

	public TimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
