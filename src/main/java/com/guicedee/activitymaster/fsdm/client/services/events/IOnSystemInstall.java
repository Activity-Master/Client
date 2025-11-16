package com.guicedee.activitymaster.fsdm.client.services.events;

import com.guicedee.guicedinjection.interfaces.IDefaultService;

/**
 * Event signal for systems installing
 * @param <J>
 */
public interface IOnSystemInstall<J extends IOnSystemInstall<J>> extends IDefaultService<J>
{
	void onSystemInstallStart(String systemName);
	void onSystemInstallEnd(String systemName);
	void onSystemInstallFail(String systemName);
	
}
