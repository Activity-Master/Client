package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class InvolvedPartyException
		extends ActivityMasterException
{
	public InvolvedPartyException()
	{
	}

	public InvolvedPartyException(String message)
	{
		super(message);
	}

	public InvolvedPartyException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvolvedPartyException(Throwable cause)
	{
		super(cause);
	}

	public InvolvedPartyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
