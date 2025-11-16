package com.guicedee.activitymaster.fsdm.client.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface ITimeService
{
	String TimeSystemName = "TimeSystem";
	int getDayID(LocalDateTime dateTime);

	int getDayID(LocalDate dateTime);

	int getDayID(Date dateTime);

	int getHourID(LocalDateTime dateTime);

	int getHourID(LocalDate dateTime);

	int getHourID(Date dateTime);

	int getMinuteID(LocalDateTime dateTime);

	int getMinuteID(LocalDate dateTime);

	int getMinuteID(Date dateTime);

}
