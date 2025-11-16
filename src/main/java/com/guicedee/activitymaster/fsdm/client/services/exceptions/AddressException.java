package com.guicedee.activitymaster.fsdm.client.services.exceptions;

public class AddressException
		extends ActivityMasterException
{
	public AddressException()
	{
	}

	public AddressException(String message)
	{
		super(message);
	}

	public AddressException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public AddressException(Throwable cause)
	{
		super(cause);
	}

	public AddressException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
