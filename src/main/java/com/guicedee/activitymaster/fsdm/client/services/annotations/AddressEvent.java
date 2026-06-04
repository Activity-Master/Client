package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save a change to addresses in the current running event.
 * It specifies the action taken on the address and an optional classification.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface AddressEvent
{
	/**
	 * The action performed on the address.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The classification name associated with the address event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
