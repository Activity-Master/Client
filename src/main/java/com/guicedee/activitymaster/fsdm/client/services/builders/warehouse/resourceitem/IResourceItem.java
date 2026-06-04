package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageResourceItemTypes;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


/**
 * Warehouse table interface for Resource Item entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IResourceItem<J extends IResourceItem<J, Q>,
		Q extends IResourceItemQueryBuilder<Q, J>>
		extends IWarehouseBaseTable<J, Q, UUID>,
		        IContainsEnterprise<J>,
		        IContainsActiveFlags<J>,
		        IContainsSystem<J>,
		        IContainsData<J>,
		        IContainsHierarchy<J,java.util.UUID>,
		        IManageClassifications<J>,
		        IManageResourceItemTypes<J>
{

	/**
	 * Retrieves the filename associated with this resource item.
	 *
	 * @param session The reactive session
	 * @return A Uni containing the filename
	 */
	Uni<String> getFilename(Mutiny.Session session);

	/**
	 * Retrieves the data row for this resource item.
	 *
	 * @param session       The reactive session
	 * @param identityToken Security tokens
	 * @return A Uni containing the resource data
	 */
	Uni<IResourceData<?,?,?>> getDataRow(Mutiny.Session session, UUID... identityToken);

	/**
	 * Returns the data type of the resource item.
	 *
	 * @return A Uni containing the data type
	 */
	Uni<String> getResourceItemDataType();

}
