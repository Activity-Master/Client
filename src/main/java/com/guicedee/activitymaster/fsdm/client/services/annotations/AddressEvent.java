package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Save a change to addresses in the current running event
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface AddressEvent
{
	EventAction value();
	String classificationName() default "NoClassification";
}
