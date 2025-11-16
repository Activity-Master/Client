package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IDefaultService;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

/**
 * A system or micro service registered to store and retrieve data from the database
 * @param <J>
 */
public interface IActivityMasterSystem<J extends IActivityMasterSystem<J>>
		extends IDefaultService<J>, IProgressable
{
	Uni<ISystems<?,?>> registerSystem(Mutiny.Session session, IEnterprise<?,?> enterprise);
	
	Uni<Void> createDefaults(Mutiny.Session session, IEnterprise<?,?> enterprise);

	int totalTasks();

	default Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		return Uni.createFrom().voidItem();
	}
	
	Uni<ISystems<?,?>> getSystem(Mutiny.Session session, String enterpriseName);
	
	Uni<UUID> getSystemToken(Mutiny.Session session, String enterpriseName);
	
	Uni<Boolean> hasSystemInstalled(Mutiny.Session session, IEnterprise<?,?> enterprise);
	
	String getSystemName();
	
	String getSystemDescription();
	
	@SuppressWarnings({"UnnecessaryLocalVariable", "rawtypes", "unchecked"})
	static @NotNull Set<IActivityMasterSystem<?>> allSystems()
	{
		Set iActivityMasterSystems = IGuiceContext.loaderToSet(ServiceLoader.load(IActivityMasterSystem.class));
		return iActivityMasterSystems;
	}
	
}
