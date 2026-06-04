package com.guicedee.activitymaster.fsdm.client.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Service interface for time-related calculations and identifier generation.
 */
public interface ITimeService
{
	/**
	 * The name of the Time system.
	 */
	String TimeSystemName = "TimeSystem";

	/**
	 * Gets a unique day ID for a given LocalDateTime.
	 *
	 * @param dateTime The date time to calculate the ID for
	 * @return The day ID
	 */
	int getDayID(LocalDateTime dateTime);

	/**
	 * Gets a unique day ID for a given LocalDate.
	 *
	 * @param dateTime The date to calculate the ID for
	 * @return The day ID
	 */
	int getDayID(LocalDate dateTime);

	/**
	 * Gets a unique day ID for a given Date.
	 *
	 * @param dateTime The date to calculate the ID for
	 * @return The day ID
	 */
	int getDayID(Date dateTime);

	/**
	 * Gets a unique hour ID for a given LocalDateTime.
	 *
	 * @param dateTime The date time to calculate the ID for
	 * @return The hour ID
	 */
	int getHourID(LocalDateTime dateTime);

	/**
	 * Gets a unique hour ID for a given LocalDate.
	 *
	 * @param dateTime The date to calculate the ID for
	 * @return The hour ID
	 */
	int getHourID(LocalDate dateTime);

	/**
	 * Gets a unique hour ID for a given Date.
	 *
	 * @param dateTime The date to calculate the ID for
	 * @return The hour ID
	 */
	int getHourID(Date dateTime);

	/**
	 * Gets a unique minute ID for a given LocalDateTime.
	 *
	 * @param dateTime The date time to calculate the ID for
	 * @return The minute ID
	 */
	int getMinuteID(LocalDateTime dateTime);

	/**
	 * Gets a unique minute ID for a given LocalDate.
	 *
	 * @param dateTime The date to calculate the ID for
	 * @return The minute ID
	 */
	int getMinuteID(LocalDate dateTime);

	/**
	 * Gets a unique minute ID for a given Date.
	 *
	 * @param dateTime The date to calculate the ID for
	 * @return The minute ID
	 */
	int getMinuteID(Date dateTime);

}
