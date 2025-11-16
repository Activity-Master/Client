package com.guicedee.activitymaster.fsdm.client.services.annotations;

public enum LogItemTypes
{
	Json("application/json"),
	Xml("application/xml"),
	CSV("text/csv"),
	TLDCSV("text/csv"),
	Excel("application/vnd.ms-excel"),
	ExcelX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
	Word("application/msword"),
	WordX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
	Powerpoint("application/vnd.ms-powerpoint"),
	PowerpointX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
	ZIP("application/zip"),
	JavaScript("text/javascript"),
	CSS("text/css"),
	PDF("application/pdf"),
	Text("text/plain"),
	;
	private String mimeType;
	
	LogItemTypes(String mimeType)
	{
		this.mimeType = mimeType;
	}
	
	public String getMimeType()
	{
		return mimeType;
	}
	
	public LogItemTypes setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
		return this;
	}
}
