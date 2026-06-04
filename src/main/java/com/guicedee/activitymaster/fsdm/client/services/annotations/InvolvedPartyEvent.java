package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save an involved party event to the current running event.
 * It specifies the action taken and the classification name for the involved party.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface InvolvedPartyEvent
{
	/**
	 * The action performed on the involved party.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The classification name associated with the involved party event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
