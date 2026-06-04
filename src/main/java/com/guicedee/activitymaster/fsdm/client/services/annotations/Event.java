package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as an event producer.
 * It starts an event chain with the specified event type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface Event
{
	/**
	 * Start an event chain with the given EventType name.
	 *
	 * @return The event type name
	 */
	String value();

	/**
	 * The classification to use when the event is nested within a hierarchy.
	 *
	 * @return The parent hierarchy classification name, defaults to "NoClassification"
	 */
	String parentHierarchyClassificationName() default "NoClassification";
}
