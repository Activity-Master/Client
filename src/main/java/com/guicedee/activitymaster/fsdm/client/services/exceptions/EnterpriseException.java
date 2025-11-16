package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class EnterpriseException
		extends ActivityMasterException
{
	public EnterpriseException()
	{
	}

	public EnterpriseException(String message)
	{
		super(message);
	}

	public EnterpriseException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public EnterpriseException(Throwable cause)
	{
		super(cause);
	}

	public EnterpriseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
