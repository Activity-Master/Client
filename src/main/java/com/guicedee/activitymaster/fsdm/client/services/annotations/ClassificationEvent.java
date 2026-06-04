package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save a classification event to the current running event.
 * It specifies the action taken and the classification name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface ClassificationEvent
{
	/**
	 * The action performed on the classification.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The classification name associated with the event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
