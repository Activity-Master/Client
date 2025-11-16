package com.guicedee.activitymaster.fsdm.client.services;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface for Activity Master service.
 * This interface provides methods for managing the Activity Master system.
 */
public interface IActivityMasterService<J extends IActivityMasterService<J>>
{
	/**
	 * Loads systems for the specified enterprise.
	 *
	 * @param session
	 * @param enterpriseName the name of the enterprise
	 * @return a Uni that completes when the systems are loaded
	 */
	Uni<Void> loadSystems(Mutiny.Session session, String enterpriseName);

	/**
     * Loads updates for the specified enterprise.
     *
     * @param session
     * @param enterprise the enterprise
     * @return a Uni that completes when the updates are loaded
     */
	Uni<Void> loadUpdates(Mutiny.Session session, IEnterprise<?,?> enterprise);

	/**
	 * Runs a SQL script.
	 *
	 * @param script the SQL script to run
	 * @return a Uni that completes when the script has been executed
	 */
	Uni<Void> runScript(String script);

	/**
	 * Updates partition bases.
	 *
	 * @return a Uni that completes when the partition bases have been updated
	 * @deprecated This method is deprecated
	 */
	@Deprecated
	Uni<Void> updatePartitionBases();

	static Uni<ISystems<?, ?>> getISystem(Mutiny.Session session, Enum systemName, IEnterprise<?,?> enterprise) {
		return getISystem(session, systemName.toString(), enterprise);
	}


	/**
	 * Gets a system by name for the specified enterprise.
	 *
	 * @param session
	 * @param systemName the name of the system
	 * @param enterprise the enterprise
	 * @return a Uni that emits the system
	 */
	static Uni<ISystems<?, ?>> getISystem(Mutiny.Session session, String systemName, IEnterprise<?,?> enterprise) {
			ISystemsService<?> systemsService = com.guicedee.client.IGuiceContext.get(ISystemsService.class);
			return systemsService.findSystem(session,enterprise,systemName);
	}

 /**
  * Cache for system tokens, keyed by systemName and enterpriseId
  */
 Map<String, Map<UUID, UUID>> SYSTEM_TOKEN_CACHE = new ConcurrentHashMap<>();

 /**
  * Gets a system token by name for the specified enterprise.
  * Results are cached per systemName per enterprise.
  *
  * @param session
  * @param systemName the name of the system
  * @param enterprise the enterprise
  * @return a Uni that emits the system token
  */
 static Uni<UUID> getISystemToken(Mutiny.Session session, String systemName, IEnterprise<?,?> enterprise) {
 	// Check if we have a cached token for this system and enterprise
 	UUID enterpriseId = enterprise.getId();
 	Map<UUID, UUID> enterpriseTokens = SYSTEM_TOKEN_CACHE.computeIfAbsent(systemName, k -> new ConcurrentHashMap<>());
 	UUID cachedToken = enterpriseTokens.get(enterpriseId);
	
 	if (cachedToken != null) {
 		return Uni.createFrom().item(cachedToken);
 	}
	
 	// If not cached, fetch from database and cache the result
 	ISystemsService<?> systemsService = com.guicedee.client.IGuiceContext.get(ISystemsService.class);
 	return getISystem(session, systemName, enterprise).chain(system -> {
 		return systemsService.getSecurityIdentityToken(session, system).onItem().invoke(token -> {
 			if (token != null) {
 				enterpriseTokens.put(enterpriseId, token);
 			}
 		});
 	});
 }
}
