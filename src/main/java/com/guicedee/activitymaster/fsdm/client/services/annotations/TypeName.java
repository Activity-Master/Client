package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Marker annotation for specifying a relevant type name.
 * Typically used on parameters or methods to provide a dynamic type name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Documented
public @interface TypeName
{
}
