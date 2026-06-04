package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;

import java.io.Serializable;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

/**
 * Query builder interface for entities with an 'enterpriseID' field.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier
 */
public interface IQueryBuilderEnterprise<
		J extends IQueryBuilderEnterprise<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>,
		I extends UUID>
		extends IQueryBuilderDefault<J, E, I>
{
	/**
	 * Filters by the enterprise associated with the given system.
	 *
	 * @param system The system to extract the enterprise from
	 * @return This builder
	 */
	default J withEnterprise(ISystems<?, ?> system)
	{
		return withEnterprise(system.getEnterprise());
	}

	/**
	 * Filters by the specified enterprise entity.
	 *
	 * @param enterprise The enterprise to filter by
	 * @return This builder
	 */
	default J withEnterprise(IEnterprise<?, ?> enterprise)
	{
		if (enterprise != null && enterprise.getId() != null)
		{
			where(getAttribute("enterpriseID"), Equals, enterprise);
		}
		return (J) this;
	}
}
