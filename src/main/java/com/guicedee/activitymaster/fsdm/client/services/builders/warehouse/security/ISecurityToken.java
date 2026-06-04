package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;

//@Schema(name = "SecurityToken")
/**
 * Warehouse table interface for Security Token entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface ISecurityToken<J extends ISecurityToken<J, Q>,
		Q extends ISecurityTokenQueryBuilder<Q, J>>
		extends IContainsActiveFlags<J>,
		        IContainsEnterprise<J>,
		        IContainsSystem<J>,
		        IWarehouseBaseTable<J,Q, UUID>,
		        IContainsNameAndDescription<J>,
		        IContainsHierarchy<J,java.util.UUID>

{
	/**
	 * Returns the security token string.
	 *
	 * @return The security token
	 */
	String getSecurityToken();
}
