package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save a geography event to the current running event.
 * It specifies the action taken and the geography classification name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface GeographyEvent
{
	/**
	 * The action performed on the geography.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The classification name associated with the geography event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
