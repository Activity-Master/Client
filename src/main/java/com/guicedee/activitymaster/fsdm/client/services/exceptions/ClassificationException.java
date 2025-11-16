package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class ClassificationException
		extends ActivityMasterException
{
	public ClassificationException()
	{
	}

	public ClassificationException(String message)
	{
		super(message);
	}

	public ClassificationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ClassificationException(Throwable cause)
	{
		super(cause);
	}

	public ClassificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
