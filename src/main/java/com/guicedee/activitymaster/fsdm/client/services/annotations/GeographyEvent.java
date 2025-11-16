package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface GeographyEvent
{
	EventAction value();
	String classificationName() default "NoClassification";
}
