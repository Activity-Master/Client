package com.guicedee.activitymaster.fsdm.client.services.classifications.types;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author mmagon
 */
public enum DateTimeFormats
{

	YearIDFormat("yyyy"),
	YearShortFormat("yy"),
	YearYYYFormat("''yy"),
	YearFullFormat("yyyy"),

	DayDateFormat("dd/MM/yyyy"),
	DayInMonthFormat("dd"),
	DayIDFormat("yyyyMMdd"),
	DaySlashIDFormat("yyyy/MM/dd"),
	DayDDMMYYYYFormat("ddMMyyyy"),
	DaySlashDDMMYYYYFormat("dd/MM/yyyy"),
	DayHyphenDDMMYYYYFormat("dd-MM-yyyy"),

	DayLongDescriptionFormat("EEE, MMM d, ''yy"),
	DayFullDescriptionFormat("EEE, MMMM dd, ''yy"),
	DayMonthDescriptionFormat("d MMM yyyy"),
	DayYearFirstDescriptionFormat("YYYY/MM/dd"),

	MonthIDFormat("yyyyMM"),
	MonthNumberFormat("MM"),
	MonthDescriptionFormat("MMM yyyy"),
	MonthLongDescriptionFormat("d MMM yyyy"),
	MonthMMYYDescriptionFormat("MM ''yy"),
	MonthMMMMYYDescriptionFormat("MMM ''yy"),
	MonthMMYYYYDescriptionFormat("MM yyyy"),
	MonthMonthYYDescriptionFormat("MMMM ''yy"),
	MonthMonthNameYYYYDescriptionFormat("MMMM yyyy"),
	MonthShortDescriptionFormat("MMM");

	private String format;
	private DateTimeFormatter dateTimeFormatter;

	private DateTimeFormats(String format)
	{
		this.format = format;
		dateTimeFormatter = DateTimeFormatter.ofPattern(format);
	}

	/**
	 * Returns a thread safe instance of simple date format
	 *
	 * @return
	 */
	public SimpleDateFormat getSimpleDateFormat()
	{
		return new SimpleDateFormat(format);
	}

	/**
	 * Returns the cached version of date time formatter
	 *
	 * @return
	 */
	public DateTimeFormatter getDateFormatter()
	{
		return dateTimeFormatter;
	}

	public String getFormat()
	{
		return format;
	}

	/**
	 * Attempts to convert,
	 *
	 * @param localDateTime
	 * 		a string to parse
	 *
	 * @return Null if no value, swallows date time parse exception
	 */
	public LocalDateTime from(String localDateTime)
	{
		try
		{
			return LocalDateTime.parse(localDateTime);
		}
		catch (DateTimeParseException dte)
		{
			return null;
		}
	}

	public static LocalDateTime fromString(String localDateTime, DateTimeFormats format)
	{
		return format.from(localDateTime);
	}

	/**
	 * Attempts to parse a string into a local date time, in enum declaration order, to a local date time
	 * <p>
	 * Prefer to use the actual from() method
	 *
	 * @param localDateTime
	 *
	 * @return
	 */
	public static LocalDateTime fromString(String localDateTime)
	{
		for (DateTimeFormats value : DateTimeFormats.values())
		{
			LocalDateTime ldt = value.from(localDateTime);
			if (ldt != null)
			{
				return ldt;
			}
		}
		return null;
	}
}
