package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save a rules event to the current running event.
 * It specifies the action taken, the rule set type, and an optional classification.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface RulesEvent
{
	/**
	 * The action performed on the rules.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The type of rule set.
	 *
	 * @return The rule set type
	 */
	String type();

	/**
	 * The classification name associated with the rules event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
