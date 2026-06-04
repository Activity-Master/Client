package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Default query builder interface for Activity Master entities.
 * Extends SCD capabilities for standard warehouse tables.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier (usually UUID)
 */
public interface IQueryBuilderDefault<J extends IQueryBuilderDefault<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>,
		I extends UUID>
		extends IQueryBuilderSCD<J, E, I>
{

}
