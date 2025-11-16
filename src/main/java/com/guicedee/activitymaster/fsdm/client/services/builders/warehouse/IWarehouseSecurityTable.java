package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSecurity;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

public interface IWarehouseSecurityTable<
		J extends IWarehouseSecurityTable<J, Q,I>,
		Q extends IQueryBuilderSecurity<Q, J, I>,
		I extends UUID
		>
		extends IWarehouseBaseTable<J, Q, I>
{
	@NotNull
	boolean isCreateAllowed();
	
	J setCreateAllowed(@NotNull boolean createAllowed);
	
	@NotNull
	boolean isUpdateAllowed();
	
	J setUpdateAllowed(@NotNull boolean updateAllowed);
	
	@NotNull
	boolean isDeleteAllowed();
	
	J setDeleteAllowed(@NotNull boolean deleteAllowed);
	
	@NotNull
	boolean isReadAllowed();
	
	J setReadAllowed(@NotNull boolean readAllowed);
	
	ISecurityToken<?, ?> getSecurityTokenID();
	
	J setSecurityTokenID(ISecurityToken<?, ?> securityTokenID);
	
	IActiveFlag<?, ?> getActiveFlagID();
	
	J setActiveFlagID(IActiveFlag<?, ?> activeFlagID);
	
	IEnterprise<?, ?> getEnterpriseID();
	
	J setEnterpriseID(IEnterprise<?, ?> enterpriseID);
	
	ISystems<?, ?> getSystemID();
	
	J setSystemID(ISystems<?, ?> systemID);
	
	@NotNull
	UUID getOriginalSourceSystemUniqueID();
	
	J setOriginalSourceSystemUniqueID(@NotNull UUID originalSourceSystemUniqueID);
	
	UUID getOriginalSourceSystemID();
	
	J setOriginalSourceSystemID(UUID originalSourceSystemID);
}
