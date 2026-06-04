package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseSecurityTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;

import java.util.UUID;


/**
 * Core warehouse table interface that includes support for row-level security.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 * @param <I> The identifier type
 * @param <S> The security table type
 */
public interface IWarehouseCoreTable<
		J extends IWarehouseCoreTable<J, Q, I,S>,
		Q extends IQueryBuilderDefault<Q, J, I>,
		I extends UUID,
		S extends IWarehouseSecurityTable<S,?,?>
		>
		extends IWarehouseBaseTable<J,Q,I>
{
	/**
	 * Creates a default security record for this entity in the specified system.
	 *
	 * @param session       The reactive session
	 * @param system        The system the entity belongs to
	 * @param identityToken Security tokens for the owner
	 * @return A Uni that completes when the security record is created
	 */
	Uni<Void> createDefaultSecurity(org.hibernate.reactive.mutiny.Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);
	
}
