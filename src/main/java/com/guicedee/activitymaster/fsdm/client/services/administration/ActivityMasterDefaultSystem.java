package com.guicedee.activitymaster.fsdm.client.services.administration;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;
//import jakarta.transaction.Transactional;

import java.util.UUID;


/**
 * A default registered micro service/system used to provide access and api's
 *
 * @param <J>
 */
@SuppressWarnings("rawtypes")
@Log4j2
public abstract class ActivityMasterDefaultSystem<J extends ActivityMasterDefaultSystem<J>>
        implements IActivityMasterSystem<J>
{
    @Inject
    private ISystemsService<?> systemsService;

    @Inject
    private IEnterpriseService<?> enterpriseService;

    @Override
    public Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?, ?> enterprise)
    {
        log.debug("🚀 Starting post-startup tasks for enterprise {}", enterprise.getId());
        
        return session.withTransaction(tx -> {
            log.debug("📋 Using provided session for enterprise {}", enterprise.getId());

            // Execute tasks sequentially to follow reactivity rules
            return getSystem(session, enterprise)
                    .invoke(() -> log.debug("✅ getSystem completed for {}", enterprise.getId()))
                    .chain(system -> getSystemToken(session, enterprise)
                            .invoke(() -> log.debug("✅ getSystemToken completed for {}", enterprise.getId()))
                            .replaceWithVoid());
        })
        .invoke(() -> log.debug("✅ Transaction committed for {}", enterprise.getId()))
        .onFailure().invoke(e -> log.error("❌ Startup failed for {} - {}", enterprise.getId(), e.getMessage(), e))
        .replaceWith(Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Boolean> hasSystemInstalled(Mutiny.Session session, IEnterprise<?, ?> enterprise)
    {
        log.debug("🔍 Checking if system '{}' is installed for enterprise '{}'", getSystemName(), enterprise.getName());
        
        return systemsService.doesSystemExist(session, enterprise, getSystemName())
                .onItem().invoke(exists -> {
                    if (exists) {
                        log.debug("✅ System '{}' is installed for enterprise '{}'", getSystemName(), enterprise.getName());
                    } else {
                        log.debug("❌ System '{}' is not installed for enterprise '{}'", getSystemName(), enterprise.getName());
                    }
                })
                .onFailure().invoke(error -> 
                    log.error("❌ Error checking if system '{}' is installed: {}", getSystemName(), error.getMessage(), error));
    }

    public Uni<ISystems<?, ?>> getSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise)
    {
        for (IActivityMasterSystem<?> allSystem : IActivityMasterSystem.allSystems())
        {
            if (allSystem.getSystemName()
                        .equals(getSystemName()))
            {
                return systemsService.findSystem(session, enterprise, getSystemName());
            }
        }
        return null;
    }

    public Uni<UUID> getSystemToken(Mutiny.Session session, IEnterprise<?, ?> enterprise)
    {
        for (IActivityMasterSystem<?> allSystem : IActivityMasterSystem.allSystems())
        {
            if (allSystem.getSystemName()
                        .equals(getSystemName()))
            {
                return getSystem(session,enterprise).chain(system->{
                    if(system!=null){
                        return systemsService.getSecurityIdentityToken(session, system);
                    }else{
                        return Uni.createFrom().nullItem();
                    }
                });
            }
        }
        return null;
    }

    public abstract String getSystemName();

    public abstract String getSystemDescription();

    public Uni<ISystems<?,?>> getSystem(Mutiny.Session session, String enterpriseName)
    {
        log.debug("🔍 Getting system '{}' for enterprise name '{}'", getSystemName(), enterpriseName);
        
        return enterpriseService
                .resolveEnterpriseIdByName(session, enterpriseName)
                .chain(id -> enterpriseService.getEnterprise(session, id))
                .onItem().invoke(enterprise -> 
                    log.debug("✅ Found enterprise '{}' with ID: {}", enterprise.getName(), enterprise.getId()))
                .onFailure().invoke(error -> 
                    log.error("❌ Error finding enterprise '{}': {}", enterpriseName, error.getMessage(), error))
                .chain(enterprise -> getSystem(session, enterprise)
                        .onItem().invoke(system -> {
                            if (system != null) {
                                log.debug("✅ Found system '{}' for enterprise '{}'", getSystemName(), enterprise.getName());
                            } else {
                                log.debug("❌ System '{}' not found for enterprise '{}'", getSystemName(), enterprise.getName());
                            }
                        })
                        .onFailure().invoke(error -> 
                            log.error("❌ Error getting system '{}' for enterprise '{}': {}", 
                                getSystemName(), enterprise.getName(), error.getMessage(), error))
                );
    }

    public Uni<UUID> getSystemToken(Mutiny.Session session, String enterpriseName)
    {
        log.debug("🔑 Getting system token for '{}' with enterprise name '{}'", getSystemName(), enterpriseName);
        
        return enterpriseService
                .resolveEnterpriseIdByName(session, enterpriseName)
                .chain(id -> enterpriseService.getEnterprise(session, id))
                .onItem().invoke(enterprise -> 
                    log.debug("✅ Found enterprise '{}' with ID: {}", enterprise.getName(), enterprise.getId()))
                .onFailure().invoke(error -> 
                    log.error("❌ Error finding enterprise '{}': {}", enterpriseName, error.getMessage(), error))
                .chain(enterprise -> getSystemToken(session, enterprise)
                        .onItem().invoke(token -> {
                            if (token != null) {
                                log.debug("✅ Found system token for '{}' in enterprise '{}'", getSystemName(), enterprise.getName());
                            } else {
                                log.debug("❌ System token not found for '{}' in enterprise '{}'", getSystemName(), enterprise.getName());
                            }
                        })
                        .onFailure().invoke(error -> 
                            log.error("❌ Error getting system token for '{}' in enterprise '{}': {}", 
                                getSystemName(), enterprise.getName(), error.getMessage(), error))
                );
    }
}
