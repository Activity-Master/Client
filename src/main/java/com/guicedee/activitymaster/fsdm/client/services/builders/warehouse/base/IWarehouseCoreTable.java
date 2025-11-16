package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseSecurityTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;

import java.util.UUID;


public interface IWarehouseCoreTable<
		J extends IWarehouseCoreTable<J, Q, I,S>,
		Q extends IQueryBuilderDefault<Q, J, I>,
		I extends UUID,
		S extends IWarehouseSecurityTable<S,?,?>
		>
		extends IWarehouseBaseTable<J,Q,I>
{
	Uni<Void> createDefaultSecurity(org.hibernate.reactive.mutiny.Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);
	
}
