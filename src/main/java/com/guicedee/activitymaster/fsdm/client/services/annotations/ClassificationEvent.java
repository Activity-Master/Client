package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Saves a classification event to the current running event
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface ClassificationEvent
{
	EventAction value();
	String classificationName() default "NoClassification";
}
