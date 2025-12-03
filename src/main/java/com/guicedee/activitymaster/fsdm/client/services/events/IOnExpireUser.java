package com.guicedee.activitymaster.fsdm.client.services.events;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.client.services.IDefaultService;

import java.io.Serializable;

public interface IOnExpireUser<J extends IOnExpireUser<J>> extends Serializable, IDefaultService<J>
{
	IInvolvedParty<?, ?> expireUser(String identityType, String identificationValue);
}
