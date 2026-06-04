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

/**
 * Interface for security tables that define row-level permissions for entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 * @param <I> The identifier type
 */
public interface IWarehouseSecurityTable<
		J extends IWarehouseSecurityTable<J, Q,I>,
		Q extends IQueryBuilderSecurity<Q, J, I>,
		I extends UUID
		>
		extends IWarehouseBaseTable<J, Q, I>
{
	/**
	 * Checks if creation is allowed for the associated security token.
	 *
	 * @return true if allowed
	 */
	@NotNull
	boolean isCreateAllowed();

	/**
	 * Sets whether creation is allowed.
	 *
	 * @param createAllowed true to allow
	 * @return This entity
	 */
	J setCreateAllowed(@NotNull boolean createAllowed);

	/**
	 * Checks if updates are allowed.
	 *
	 * @return true if allowed
	 */
	@NotNull
	boolean isUpdateAllowed();

	/**
	 * Sets whether updates are allowed.
	 *
	 * @param updateAllowed true to allow
	 * @return This entity
	 */
	J setUpdateAllowed(@NotNull boolean updateAllowed);

	/**
	 * Checks if deletion is allowed.
	 *
	 * @return true if allowed
	 */
	@NotNull
	boolean isDeleteAllowed();

	/**
	 * Sets whether deletion is allowed.
	 *
	 * @param deleteAllowed true to allow
	 * @return This entity
	 */
	J setDeleteAllowed(@NotNull boolean deleteAllowed);

	/**
	 * Checks if reading is allowed.
	 *
	 * @return true if allowed
	 */
	@NotNull
	boolean isReadAllowed();

	/**
	 * Sets whether reading is allowed.
	 *
	 * @param readAllowed true to allow
	 * @return This entity
	 */
	J setReadAllowed(@NotNull boolean readAllowed);

	/**
	 * Returns the associated security token.
	 *
	 * @return The security token
	 */
	ISecurityToken<?, ?> getSecurityTokenID();

	/**
	 * Sets the associated security token.
	 *
	 * @param securityTokenID The token to associate
	 * @return This entity
	 */
	J setSecurityTokenID(ISecurityToken<?, ?> securityTokenID);

	/**
	 * Returns the associated active flag.
	 *
	 * @return The active flag
	 */
	IActiveFlag<?, ?> getActiveFlagID();

	/**
	 * Sets the associated active flag.
	 *
	 * @param activeFlagID The flag to associate
	 * @return This entity
	 */
	J setActiveFlagID(IActiveFlag<?, ?> activeFlagID);

	/**
	 * Returns the associated enterprise.
	 *
	 * @return The enterprise
	 */
	IEnterprise<?, ?> getEnterpriseID();

	/**
	 * Sets the associated enterprise.
	 *
	 * @param enterpriseID The enterprise to associate
	 * @return This entity
	 */
	J setEnterpriseID(IEnterprise<?, ?> enterpriseID);

	/**
	 * Returns the associated system.
	 *
	 * @return The system
	 */
	ISystems<?, ?> getSystemID();

	/**
	 * Sets the associated system.
	 *
	 * @param systemID The system to associate
	 * @return This entity
	 */
	J setSystemID(ISystems<?, ?> systemID);

	/**
	 * Returns the unique ID of the original source system for this record.
	 *
	 * @return The original source system unique ID
	 */
	@NotNull
	UUID getOriginalSourceSystemUniqueID();

	/**
	 * Sets the unique ID of the original source system.
	 *
	 * @param originalSourceSystemUniqueID The unique ID to set
	 * @return This entity
	 */
	J setOriginalSourceSystemUniqueID(@NotNull UUID originalSourceSystemUniqueID);

	/**
	 * Returns the ID of the original source system.
	 *
	 * @return The original source system ID
	 */
	UUID getOriginalSourceSystemID();

	/**
	 * Sets the ID of the original source system.
	 *
	 * @param originalSourceSystemID The ID to set
	 * @return This entity
	 */
	J setOriginalSourceSystemID(UUID originalSourceSystemID);
}
