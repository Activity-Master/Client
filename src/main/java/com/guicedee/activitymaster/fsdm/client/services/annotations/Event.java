package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface Event
{
	/**
	 * Start an event chain with the given EventType name
	 * @return
	 */
	String value();
	
	/**
	 * The classification to use when the event is nested
	 * @return
	 */
	String parentHierarchyClassificationName() default "NoClassification";
}
