package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;

import java.io.Serializable;
import java.util.UUID;

//@Schema(name = "SecurityToken")
public interface ISecurityToken<J extends ISecurityToken<J, Q>,
		Q extends ISecurityTokenQueryBuilder<Q, J>>
		extends IContainsActiveFlags<J>,
		        IContainsEnterprise<J>,
		        IContainsSystem<J>,
		        IWarehouseBaseTable<J,Q, UUID>,
		        IContainsNameAndDescription<J>,
		        IContainsHierarchy<J,java.util.UUID>

{
	String getSecurityToken();
}
