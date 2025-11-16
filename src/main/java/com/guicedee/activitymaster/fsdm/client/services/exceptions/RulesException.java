package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class RulesException
		extends ActivityMasterException
{
	public RulesException()
	{
	}

	public RulesException(String message)
	{
		super(message);
	}

	public RulesException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RulesException(Throwable cause)
	{
		super(cause);
	}

	public RulesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
