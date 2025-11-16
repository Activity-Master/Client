package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.*;

/**
 * A date in yyyy/mm/dd format that sorts updates accordingly
 */
@Target(
		{
				ElementType.TYPE, ElementType.TYPE_USE, ElementType.PARAMETER
		})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@BindingAnnotation
public @interface SortedUpdate
{
	/**
	 * int that defines the order the update must run, the same int overrides any previous value,make sure to keep unique and in order
	 * @return
	 */
	int sortOrder();
	
	/**
	 * The number of tasks this update performs
	 * @return
	 */
	int taskCount();
	
	/**
	 * If this update can be run after the install
	 * @return
	 */
	boolean optional() default false;
	
	/**
	 * If this update is forced to run
	 * @return
	 */
	boolean force() default false;
}
