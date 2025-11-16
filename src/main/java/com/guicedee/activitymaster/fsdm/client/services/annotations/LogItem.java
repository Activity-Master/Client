package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

import static com.guicedee.activitymaster.fsdm.client.services.annotations.LogItemTypes.*;

/**
 * Marks a parameter as recordable for an event,
 * and saves the marked object down as a resource item link to the current executing event
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Inherited
@Documented
public @interface LogItem
{
	/**
	 * The classification name to assign to the resource item
	 * @return
	 */
	String value();
	
	/**
	 * The log item type
	 * @return
	 */
	LogItemTypes type() default Json;
}
