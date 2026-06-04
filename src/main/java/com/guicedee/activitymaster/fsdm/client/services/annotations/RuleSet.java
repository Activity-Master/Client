package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Marker for the rule set object, only use on parameters or methods (for the return value).
 * This annotation is used to associate a specific rule set classification name with a method return or parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Documented
public @interface RuleSet
{
	/**
	 * The classification name to assign to the rule set.
	 *
	 * @return The classification name
	 */
	String value();
}
