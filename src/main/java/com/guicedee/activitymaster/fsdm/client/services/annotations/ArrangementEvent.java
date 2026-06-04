package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save a change to arrangements in the current running event.
 * It specifies the action taken, the arrangement type, and an optional classification.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface ArrangementEvent
{
	/**
	 * The action performed on the arrangement.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The type of arrangement.
	 *
	 * @return The arrangement type
	 */
	String type();

	/**
	 * The classification name associated with the arrangement event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
