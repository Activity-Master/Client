package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Marker for the arrangement object, only use on parameters or methods (for the return value).
 * This annotation is used to associate a specific arrangement type name with a method return or parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Documented
public @interface Arrangement
{
	/**
	 * The arrangement type name to assign.
	 *
	 * @return The arrangement type name
	 */
	String value();
}
