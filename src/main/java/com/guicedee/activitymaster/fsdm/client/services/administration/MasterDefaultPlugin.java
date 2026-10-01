package com.guicedee.activitymaster.fsdm.client.services.administration;

import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IMasterPlugin;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import java.util.UUID;

/** Independent plugin lifecycle. A durable Systems row is a registration ID, not System authority. */
public abstract class MasterDefaultPlugin<J extends MasterDefaultPlugin<J>> implements IMasterPlugin<J> {
    @Override public Uni<ISystems<?, ?>> registerSystem(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        if (ISystemsService.ActivityMasterSystemName.equals(getSystemName()))
            return Uni.createFrom().failure(new SecurityException("A plugin cannot register as ActivityMaster Core"));
        ISystemsService<?> systems = IGuiceContext.get(ISystemsService.class);
        return systems.create(session, enterprise, getSystemName(), getSystemDescription())
                .chain(plugin -> systems.registerNewPlugin(session, enterprise, plugin).replaceWith(plugin));
    }
    public Uni<ISystems<?, ?>> getSystem(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        return IGuiceContext.get(ISystemsService.class).findSystem(session, enterprise, getSystemName());
    }
    @Override public Uni<ISystems<?, ?>> getSystem(Mutiny.StatelessSession session, String enterpriseName) {
        IEnterpriseService<?> enterprises = IGuiceContext.get(IEnterpriseService.class);
        return enterprises.resolveEnterpriseIdByName(session, enterpriseName)
                .chain(id -> enterprises.getEnterprise(session, id)).chain(enterprise -> getSystem(session, enterprise));
    }
    public Uni<UUID> getSystemToken(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise) {
        return getSystem(session, enterprise).chain(plugin ->
                IGuiceContext.get(ISystemsService.class).getSecurityIdentityToken(session, plugin));
    }
    @Override public Uni<UUID> getSystemToken(Mutiny.StatelessSession session, String enterpriseName) {
        return getSystem(session, enterpriseName).chain(plugin ->
                IGuiceContext.get(ISystemsService.class).getSecurityIdentityToken(session, plugin));
    }
}
