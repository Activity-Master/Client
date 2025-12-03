package com.guicedee.activitymaster.fsdm.client.services.events;

import com.guicedee.client.services.IDefaultService;

/**
 * Event signals for systems updating
 * @param <J>
 */
public interface IOnSystemUpdate<J extends IOnSystemUpdate<J>> extends IDefaultService<J>
{
	void onSystemUpdateStart(Class<?> systemClass);
	void onSystemUpdateEnd(Class<?> systemClass);
	void onSystemUpdateFail(Class<?> systemClass);
	
}
