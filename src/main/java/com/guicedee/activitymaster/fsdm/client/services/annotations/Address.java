package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Marker for the address object, only use on parameters or methods (for the return value).
 * This annotation is used to associate a specific address classification with a method return or parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Documented
public @interface Address
{
	/**
	 * The classification name to assign to the address.
	 *
	 * @return The classification name
	 */
	String value();
}
