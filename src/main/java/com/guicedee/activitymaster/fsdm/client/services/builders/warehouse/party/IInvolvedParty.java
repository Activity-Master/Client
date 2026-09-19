package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.*;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.*;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


//@Schema(description = "The UUID Representation of this interface",name = "InvolvedParty")
/**
 * Warehouse table interface for Involved Party entities.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 */
public interface IInvolvedParty<J extends IInvolvedParty<J,Q>, Q extends IInvolvedPartyQueryBuilder<Q,J>>
		extends IWarehouseBaseTable<J,Q, UUID>,
		        IManageClassifications<J>,
		        IManageResourceItems<J>,
		        IManageAddresses<J>,
		        IContainsHierarchy<J,java.util.UUID>,
		        IManagePartyIdentificationTypes<J>,
		        IManagePartyTypes<J>,
		        IManagePartyNameTypes<J>,
		        IManageProducts<J>,
		        IManageProductTypes<J>,
		        IContainsEnterprise<J>,
		        IManageRules<J>,
		        IContainsRowRecordInformation<J>
{
	/** Stateless variant of {@link #getSecurityIdentity(Mutiny.StatelessSession)}. */
	Uni<UUID> getSecurityIdentity(Mutiny.StatelessSession session);
}
