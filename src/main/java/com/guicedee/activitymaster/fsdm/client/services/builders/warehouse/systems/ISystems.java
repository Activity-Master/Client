package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseNameAndDescriptionTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.IManageClassifications;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsActiveFlags;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsId;

import java.io.Serializable;
import java.util.UUID;


//@Schema(name = "System")
public interface ISystems<J extends ISystems<J, Q>,
                                 Q extends ISystemsQueryBuilder<Q, J>
                                 >
        extends IWarehouseNameAndDescriptionTable<J, Q, UUID>,
                        IContainsEnterprise<J>,
                        IContainsActiveFlags<J>,
                        IManageClassifications<J>,
                        IWarehouseBaseTable<J, Q, UUID>,
                        IContainsId<J>
{

}
