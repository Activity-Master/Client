package com.guicedee.activitymaster.fsdm.client.services.annotations;

/**
 * Defines the possible actions that can be performed during an event.
 */
public enum EventAction
{
	/**
	 * A new object was created.
	 */
	Created,

	/**
	 * An object was added to a collection or relationship.
	 */
	Added,

	/**
	 * An existing object was updated.
	 */
	Updated,

	/**
	 * An object was deleted.
	 */
	Deleted,

	/**
	 * An object was used or accessed.
	 */
	Used
}
