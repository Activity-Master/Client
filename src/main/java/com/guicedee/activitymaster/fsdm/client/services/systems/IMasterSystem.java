package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.IDefaultService;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

/**
 * A system or micro service registered to store and retrieve data from the database
 * @param <J>
 */
public interface IMasterSystem<J extends IMasterSystem<J>>
		extends IDefaultService<J>, IProgressable
{
	Uni<ISystems<?,?>> registerSystem(Mutiny.Session session, IEnterprise<?,?> enterprise);
	
	Uni<Void> createDefaults(Mutiny.Session session, IEnterprise<?,?> enterprise);

	/**
	 * Stateless variant of {@link #createDefaults(Mutiny.Session, IEnterprise)}.
	 * <p>
	 * Default implementation signals "not yet converted" via {@link UnsupportedOperationException}; systems
	 * that have a genuine stateless default-provisioning path (composed from the prepped stateless readers,
	 * the stateless {@code IClassificationService.create}, and the stateless default-security writes)
	 * override this. The install loop can prefer the stateless overload where available and fall back to the
	 * {@link Mutiny.Session} path otherwise.
	 */
	default Uni<Void> createDefaults(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise)
	{
		return Uni.createFrom().failure(new UnsupportedOperationException(
				"System '" + getSystemName() + "' has no stateless createDefaults; use the Mutiny.Session overload"));
	}

	int totalTasks();

	default Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		return Uni.createFrom().voidItem();
	}
	
	Uni<ISystems<?,?>> getSystem(Mutiny.Session session, String enterpriseName);
	
	Uni<UUID> getSystemToken(Mutiny.Session session, String enterpriseName);
	
	Uni<Boolean> hasSystemInstalled(Mutiny.Session session, IEnterprise<?,?> enterprise);

	/**
	 * Stateless-session variant of {@link #hasSystemInstalled(Mutiny.Session, IEnterprise)}.
	 * <p>
	 * Resolves through the {@link ISystemsService} stateless system-existence lookup (a pure
	 * {@code Systems}-entity query), so it needs no persistence context.
	 */
	default Uni<Boolean> hasSystemInstalled(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise)
	{
		return IGuiceContext.get(ISystemsService.class).doesSystemExist(session, enterprise, getSystemName());
	}

	/**
	 * Stateless-safe resolution of this system's <em>id</em> within an enterprise — a scalar
	 * projection delegated to
	 * {@link ISystemsService#findSystemId(Mutiny.StatelessSession, IEnterprise, String, java.util.UUID...)}.
	 * <p>
	 * A managed {@code Systems} entity cannot be returned on a stateless session (it is {@code @Cacheable}
	 * with eager associations); use a {@link Mutiny.Session} via {@link #getSystem(Mutiny.Session, String)}
	 * when the entity itself is required.
	 */
	default Uni<UUID> getSystemId(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise)
	{
		return IGuiceContext.get(ISystemsService.class).findSystemId(session, enterprise, getSystemName());
	}
	
	String getSystemName();
	
	String getSystemDescription();
	
	@SuppressWarnings({"UnnecessaryLocalVariable", "rawtypes", "unchecked"})
	static @NotNull Set<IMasterSystem<?>> allSystems()
	{
		Set iActivityMasterSystems = IGuiceContext.loaderToSet(ServiceLoader.load(IMasterSystem.class));
		return iActivityMasterSystems;
	}
	
}
