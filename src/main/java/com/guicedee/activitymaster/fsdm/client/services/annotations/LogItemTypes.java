package com.guicedee.activitymaster.fsdm.client.services.annotations;

/**
 * Defines the supported types for log items, along with their associated MIME types.
 */
public enum LogItemTypes
{
	/**
	 * JSON format (application/json)
	 */
	Json("application/json"),

	/**
	 * XML format (application/xml)
	 */
	Xml("application/xml"),

	/**
	 * CSV format (text/csv)
	 */
	CSV("text/csv"),

	/**
	 * TLD CSV format (text/csv)
	 */
	TLDCSV("text/csv"),

	/**
	 * Excel format (application/vnd.ms-excel)
	 */
	Excel("application/vnd.ms-excel"),

	/**
	 * Excel OpenXML format (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
	 */
	ExcelX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

	/**
	 * Word format (application/msword)
	 */
	Word("application/msword"),

	/**
	 * Word OpenXML format (application/vnd.openxmlformats-officedocument.wordprocessingml.document)
	 */
	WordX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

	/**
	 * Powerpoint format (application/vnd.ms-powerpoint)
	 */
	Powerpoint("application/vnd.ms-powerpoint"),

	/**
	 * Powerpoint OpenXML format (application/vnd.openxmlformats-officedocument.presentationml.presentation)
	 */
	PowerpointX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),

	/**
	 * ZIP archive format (application/zip)
	 */
	ZIP("application/zip"),

	/**
	 * JavaScript format (text/javascript)
	 */
	JavaScript("text/javascript"),

	/**
	 * CSS format (text/css)
	 */
	CSS("text/css"),

	/**
	 * PDF format (application/pdf)
	 */
	PDF("application/pdf"),

	/**
	 * Plain text format (text/plain)
	 */
	Text("text/plain"),
	;

	/**
	 * The MIME type associated with the log item type.
	 */
	private String mimeType;

	/**
	 * Constructor for LogItemTypes.
	 *
	 * @param mimeType The MIME type to associate
	 */
	LogItemTypes(String mimeType)
	{
		this.mimeType = mimeType;
	}

	/**
	 * Gets the MIME type associated with this log item type.
	 *
	 * @return The MIME type string
	 */
	public String getMimeType()
	{
		return mimeType;
	}

	/**
	 * Sets the MIME type associated with this log item type.
	 *
	 * @param mimeType The MIME type to set
	 * @return This LogItemTypes instance
	 */
	public LogItemTypes setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
		return this;
	}
}
