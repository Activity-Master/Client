package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class ActiveFlagException
		extends ActivityMasterException
{
	public ActiveFlagException()
	{
	}

	public ActiveFlagException(String message)
	{
		super(message);
	}

	public ActiveFlagException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ActiveFlagException(Throwable cause)
	{
		super(cause);
	}

	public ActiveFlagException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
