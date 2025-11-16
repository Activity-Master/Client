package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageResourceItemTypes;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


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
	Uni<IResourceItem<?, ?>> updateDataTypeValue(Mutiny.Session session, String newValue);

	Uni<String> getFilename(Mutiny.Session session);

	Uni<IResourceData<?,?,?>> getDataRow(Mutiny.Session session, UUID... identityToken);

	Uni<String> getResourceItemDataType();

}
