package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;


/**
 * Warehouse table interface for Addresses.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IAddress<J extends IAddress<J, Q>,
		Q extends IAddressQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IManageClassifications<J>,
		        IManageResourceItems<J>,
		        IContainsActiveFlags<J>,
		        IContainsEnterprise<J>,
		        IContainsSystem<J>,
		        IContainsClassifications<J>,
		        IManageGeographies<J>
{
	/**
	 * Returns the address string value.
	 *
	 * @return The address value
	 */
	String getValue();

	/**
	 * Sets the address string value.
	 *
	 * @param value The value to set
	 * @return This entity
	 */
	J setValue(String value);
}
