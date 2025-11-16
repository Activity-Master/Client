package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.UUID;

public interface IQueryBuilderDefault<J extends IQueryBuilderDefault<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>,
		I extends UUID>
		extends IQueryBuilderSCD<J,E,I>
{

}
