package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface RulesEvent
{
	EventAction value();
	String type();
	String classificationName() default "NoClassification";
}
