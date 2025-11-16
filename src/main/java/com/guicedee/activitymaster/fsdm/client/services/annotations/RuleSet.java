package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Marker for the address object, only use on parameters or methods (for the return value)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Inherited
@Documented
public @interface RuleSet
{
	/**
	 * The classification name to assign
	 * @return
	 */
	String value();
}
