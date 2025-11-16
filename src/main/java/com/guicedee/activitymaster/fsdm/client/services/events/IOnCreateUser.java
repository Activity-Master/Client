package com.guicedee.activitymaster.fsdm.client.services.events;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.guicedinjection.interfaces.IDefaultService;
import io.smallrye.mutiny.Uni;

import java.io.Serializable;

public interface IOnCreateUser<J extends IOnCreateUser<J>> extends Serializable, IDefaultService<J>
{
	Uni<IInvolvedParty<?, ?>> createUser(String domain, String username, String password);
}
