package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address;

import com.guicedee.activitymaster.fsdm.client.services.builders.*;

import java.util.UUID;


public interface IAddressQueryBuilder<J extends IAddressQueryBuilder<J,E>,E extends IAddress<E,J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderValues<J,E, UUID>
{

}
