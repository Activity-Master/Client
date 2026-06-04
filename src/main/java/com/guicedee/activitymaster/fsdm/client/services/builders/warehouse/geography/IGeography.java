package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.geography;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageResourceItems;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;


/**
 * Warehouse table interface for Geography entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IGeography<J extends IGeography<J,Q>,
		Q extends IGeographyQueryBuilder<Q,J>
		>
		extends IWarehouseBaseTable<J,Q, UUID>,
		        IManageClassifications<J>,
		        IContainsHierarchy<J,java.util.UUID>,
		        IManageResourceItems<J>,
		        IContainsEnterprise<J>,
		        IContainsSystem<J>,
		        IContainsClassifications<J>,
		        IContainsNameAndDescription<J>,
		        IContainsActiveFlags<J>
{

}
