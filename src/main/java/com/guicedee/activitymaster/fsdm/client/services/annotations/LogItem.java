package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

import static com.guicedee.activitymaster.fsdm.client.services.annotations.LogItemTypes.*;

/**
 * Marks a parameter as recordable for an event.
 * It saves the marked object as a resource item link to the currently executing event.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Documented
public @interface LogItem
{
	/**
	 * The classification name to assign to the resource item.
	 *
	 * @return The classification name
	 */
	String value();

	/**
	 * The log item type.
	 *
	 * @return The log item type, defaults to Json
	 */
	LogItemTypes type() default Json;
}
