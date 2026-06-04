package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Marker annotation for specifying a classification name to use when it is not pre-defined.
 * Typically used on parameters or methods to provide a dynamic classification name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Documented
public @interface ClassificationName
{
}
