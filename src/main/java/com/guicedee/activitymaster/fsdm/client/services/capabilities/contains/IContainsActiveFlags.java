package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;

/**
 * Interface for entities that contain an active flag.
 * The active flag indicates the status of the entity (active, deleted, archived, etc.).
 * 
 * @param <J> The implementing class type for fluent method chaining
 */
public interface IContainsActiveFlags<J extends IContainsActiveFlags<J>>
{
	/**
	 * Gets the active flag associated with this entity.
	 * This method is non-reactive as it simply returns a property value.
	 * 
	 * @return The active flag
	 */
	IActiveFlag<?,?> getActiveFlagID();

	/**
	 * Sets the active flag for this entity.
	 * This method is non-reactive as it simply sets a property value.
	 * 
	 * @param activeFlagID The active flag to set
	 * @return This instance for method chaining
	 */
	J setActiveFlagID(IActiveFlag<?,?> activeFlagID);
}
