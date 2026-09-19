package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.IDefaultService;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.NoResultException;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

/**
 * A system or micro service registered to store and retrieve data from the database
 *
 * @param <J>
 */
public interface IMasterSystem<J extends IMasterSystem<J>>
        extends IDefaultService<J>, IProgressable
{
    /**
     * Stateless variant of {@link #registerSystem(Mutiny.StatelessSession, IEnterprise)} — find-or-creates this
     * system's {@code Systems} row and registers it (security token + system involved party) entirely on a
     * {@link Mutiny.StatelessSession}, composing the stateless {@code ISystemsService.create} +
     * {@code findSystem} + {@code registerNewSystem}. Systems that need bespoke registration logic override.
     */
    default Uni<ISystems<?, ?>> registerSystem(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
    {
        ISystemsService<?> systemsService = IGuiceContext.get(ISystemsService.class);
        return systemsService.create(session, enterprise, getSystemName(), getSystemDescription())
                             .chain(system -> systemsService.findSystem(session, enterprise, getSystemName())
                                                            .chain(sys -> systemsService.registerNewSystem(session, enterprise, sys))
                                                            .replaceWith(system));
    }

    /**
     * Stateless variant of {@link #createDefaults(Mutiny.StatelessSession, IEnterprise)}.
     * <p>
     * Default implementation signals <em>"this system has no stateless default-provisioning path"</em> by
     * failing with {@link UnsupportedOperationException}. The install loop
     * ({@code EnterpriseService.performSystemInstall}) treats that as the <strong>either/or</strong> seam:
     * it prefers this stateless overload and, when it is not implemented, falls back to running the managed
     * {@link #createDefaults(Mutiny.StatelessSession, IEnterprise)} overload on a bridged {@code Mutiny.StatelessSession}. A
     * system therefore implements <em>either</em> the managed overload <em>or</em> this stateless overload
     * (or both) — it must override this only when it has a genuine stateless provisioning path (composed from
     * the prepped stateless readers, the stateless {@code IClassificationService.create}, and the stateless
     * default-security writes).
     * <p>
     * It returns a <em>failed</em> {@code Uni} (not a synchronous throw) so the reactive
     * {@code onFailure(UnsupportedOperationException.class)} fallback in the install loop engages cleanly.
     */
    default Uni<Void> createDefaults(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
    {
        return Uni.createFrom()
                  .failure(new UnsupportedOperationException(
                          "System '" + getSystemName() + "' has no stateless createDefaults; falling back to the Mutiny.StatelessSession overload"));
    }

    int totalTasks();

    /**
     * Stateless variant of {@link #postStartup(Mutiny.StatelessSession, IEnterprise)} — validates the system resolves
     * on a {@link Mutiny.StatelessSession} via the scalar id projection. Systems with reactive post-startup
     * work override this.
     */
    default Uni<Void> postStartup(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
    {
        // Validate the system resolves via the scalar id projection. If it is NOT yet registered (NoResult)
        // — e.g. a system added since the database was first provisioned, or the app-boot/loadSystems flow
        // running against an existing enterprise that never installed this system — self-heal by registering
        // it now. registerSystem's own failure is NOT swallowed: it propagates so a genuine registration
        // problem surfaces loudly. We therefore never continue startup with a system that has no Systems row:
        // either it already resolved, or it was successfully registered here, or startup fails with the error.
        return getSystemId(session, enterprise).replaceWithVoid()
                                               .onFailure(NoResultException.class)
                                               .recoverWithUni(_ -> registerSystem(session, enterprise)
                                                       .onFailure()
                                                       .invoke(t -> LogManager.getLogger()
                                                                              .error("System '{}' was not registered and could not be auto-registered during post-startup: {}",
                                                                                     getSystemName(), t.getMessage(), t))
                                                       .replaceWithVoid());
    }

    Uni<ISystems<?, ?>> getSystem(Mutiny.StatelessSession session, String enterpriseName);

    Uni<UUID> getSystemToken(Mutiny.StatelessSession session, String enterpriseName);

    /**
     * Stateless-session variant of {@link #hasSystemInstalled(Mutiny.StatelessSession, IEnterprise)}.
     * <p>
     * Resolves through the {@link ISystemsService} stateless system-existence lookup (a pure
     * {@code Systems}-entity query), so it needs no persistence context.
     */
    default Uni<Boolean> hasSystemInstalled(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
    {
        return IGuiceContext.get(ISystemsService.class)
                            .doesSystemExist(session, enterprise, getSystemName());
    }

    /**
     * Stateless-safe resolution of this system's <em>id</em> within an enterprise — a scalar
     * projection delegated to
     * {@link ISystemsService#findSystemId(Mutiny.StatelessSession, IEnterprise, String, java.util.UUID...)}.
     * <p>
     * A managed {@code Systems} entity cannot be returned on a stateless session (it is {@code @Cacheable}
     * with eager associations); use a {@link Mutiny.StatelessSession} via {@link #getSystem(Mutiny.StatelessSession, String)}
     * when the entity itself is required.
     */
    default Uni<UUID> getSystemId(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
    {
        return IGuiceContext.get(ISystemsService.class)
                            .findSystemId(session, enterprise, getSystemName());
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
