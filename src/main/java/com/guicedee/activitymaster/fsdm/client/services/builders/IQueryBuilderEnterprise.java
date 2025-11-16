package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;

import java.io.Serializable;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

public interface IQueryBuilderEnterprise<
		J extends IQueryBuilderEnterprise<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>,
		I extends UUID>
		extends IQueryBuilderDefault<J, E, I>
{
	default J withEnterprise(ISystems<?, ?> system)
	{
		return withEnterprise(system.getEnterprise());
	}
	
	default J withEnterprise(IEnterprise<?, ?> enterprise)
	{
		if (enterprise != null && enterprise.getId() != null)
		{
			where(getAttribute("enterpriseID"), Equals, enterprise);
		}
		return (J) this;
	}
}
