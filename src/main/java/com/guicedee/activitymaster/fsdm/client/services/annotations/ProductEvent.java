package com.guicedee.activitymaster.fsdm.client.services.annotations;

import java.lang.annotation.*;

/**
 * Annotation to save a product event to the current running event.
 * It specifies the action taken, the product type, and an optional classification.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface ProductEvent
{
	/**
	 * The action performed on the product.
	 *
	 * @return The event action
	 */
	EventAction value();

	/**
	 * The type of product.
	 *
	 * @return The product type
	 */
	String type();

	/**
	 * The classification name associated with the product event.
	 *
	 * @return The classification name, defaults to "NoClassification"
	 */
	String classificationName() default "NoClassification";
}
