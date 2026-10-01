package com.guicedee.activitymaster.fsdm.client.services.systems;

import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.IDefaultService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import java.util.*;

/** A user-scoped extension, discovered independently; plugins must never implement IMasterSystem. */
public interface IMasterPlugin<J extends IMasterPlugin<J>> extends IDefaultService<J>, IProgressable {
    // Names retain the durable registration API; they do not confer System identity.
    Uni<ISystems<?, ?>> registerSystem(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise);
    Uni<ISystems<?, ?>> getSystem(Mutiny.StatelessSession session, String enterpriseName);
    Uni<UUID> getSystemToken(Mutiny.StatelessSession session, String enterpriseName);
    String getSystemName();
    String getSystemDescription();
    int totalTasks();
    default Uni<Void> createDefaults(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        return Uni.createFrom().voidItem();
    }
    default Uni<Void> postStartup(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        return Uni.createFrom().voidItem();
    }
    default Uni<Boolean> hasSystemInstalled(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        return IGuiceContext.get(ISystemsService.class).doesSystemExist(session, enterprise, getSystemName());
    }
    default Uni<UUID> getSystemId(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        return IGuiceContext.get(ISystemsService.class).findSystemId(session, enterprise, getSystemName());
    }
    default String getPluginTitle() { return getSystemName(); }
    default String getPluginVersion() { return "3.0.0-SNAPSHOT"; }
    /** Registered capabilities used by this extension; these declarations grant no access. */
    default Set<String> getPluginDependencies() {
        return Set.of(com.guicedee.activitymaster.fsdm.client.services.ISystemsService.ActivityMasterSystemName);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static Set<IMasterPlugin<?>> allPlugins() {
        Set<IMasterPlugin<?>> plugins = IGuiceContext.loaderToSet(ServiceLoader.load(IMasterPlugin.class));
        for (IMasterPlugin<?> plugin : plugins)
            if (plugin instanceof IMasterSystem<?> || ISystemsService.ActivityMasterSystemName.equals(plugin.getSystemName()))
                throw new IllegalStateException("A plugin cannot be an ActivityMaster System: " + plugin.getClass().getName());
        return plugins;
    }
}
