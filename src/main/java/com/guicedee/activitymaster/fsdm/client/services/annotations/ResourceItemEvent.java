package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save a resource item event to the current running event.
 * It specifies the action taken and the classification name for the resource item.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface ResourceItemEvent
{
	/**
	 * The action performed on the resource item.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The classification name associated with the resource item event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
