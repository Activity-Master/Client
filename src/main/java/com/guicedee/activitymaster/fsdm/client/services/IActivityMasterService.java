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
	 * @param session        The Mutiny session to use
	 * @param enterpriseName the name of the enterprise
	 * @return a Uni that completes when the systems are loaded
	 */
	Uni<Void> loadSystems(Mutiny.StatelessSession session, String enterpriseName);

	/**
     * Loads updates for the specified enterprise.
     *
     * @param session    The Mutiny session to use
     * @param enterprise the enterprise
     * @return a Uni that completes when the updates are loaded
     */
	Uni<Void> loadUpdates(Mutiny.StatelessSession session, IEnterprise<?,?> enterprise);

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

 /**
  * Cache for system tokens, keyed by systemName and enterpriseId
  */
 Map<String, Map<UUID, UUID>> SYSTEM_TOKEN_CACHE = new ConcurrentHashMap<>();

 // ---- Stateless (Mutiny.StatelessSession) twins of the system/token lookups ----

 /**
  * Cache of detached, prepped {@code Systems} resolved on a stateless session, keyed by systemName then
  * enterpriseId. Safe because stateless {@code findSystem} returns a fresh DETACHED entity (scalar
  * projection, no persistence context) — it carries only identity/descriptive columns and is never
  * bound to a session, so it may be reused across stateless units of work.
  */
 Map<String, Map<UUID, ISystems<?, ?>>> SYSTEM_CACHE = new ConcurrentHashMap<>();

 /**
  * Cache of detached enterprises resolved on a stateless session, keyed by enterprise name. Detached
  * identity carriers (no persistence context), safe to share across stateless units of work.
  */
 Map<String, IEnterprise<?, ?>> ENTERPRISE_CACHE = new ConcurrentHashMap<>();

 /** Stateless variant of {@link #getISystem(Mutiny.StatelessSession, Enum, IEnterprise)}. */
 static Uni<ISystems<?, ?>> getISystem(Mutiny.StatelessSession session, Enum systemName, IEnterprise<?, ?> enterprise) {
 	return getISystem(session, systemName.toString(), enterprise);
 }

 /** Stateless variant of {@link #getISystem(Mutiny.StatelessSession, String, IEnterprise)} — prepped {@code findSystem}, cached (detached). */
 static Uni<ISystems<?, ?>> getISystem(Mutiny.StatelessSession session, String systemName, IEnterprise<?, ?> enterprise) {
 	UUID enterpriseId = enterprise.getId();
 	Map<UUID, ISystems<?, ?>> bySystem = SYSTEM_CACHE.computeIfAbsent(systemName, k -> new ConcurrentHashMap<>());
 	ISystems<?, ?> cached = bySystem.get(enterpriseId);
 	if (cached != null) {
 		return Uni.createFrom().item(cached);
 	}
 	ISystemsService<?> systemsService = com.guicedee.client.IGuiceContext.get(ISystemsService.class);
 	return systemsService.findSystem(session, enterprise, systemName)
 			.onItem().invoke(system -> {
 				if (system != null && system.getId() != null) {
 					bySystem.put(enterpriseId, system);
 				}
 			});
 }

 /** Stateless, cached resolve of a detached enterprise by name. */
 static Uni<IEnterprise<?, ?>> getIEnterprise(Mutiny.StatelessSession session, String enterpriseName) {
 	IEnterprise<?, ?> cached = ENTERPRISE_CACHE.get(enterpriseName);
 	if (cached != null) {
 		return Uni.createFrom().item(cached);
 	}
 	IEnterpriseService<?> enterpriseService = com.guicedee.client.IGuiceContext.get(IEnterpriseService.class);
 	return enterpriseService.getEnterprise(session, enterpriseName)
 			.onItem().invoke(ent -> {
 				if (ent != null && ent.getId() != null) {
 					ENTERPRISE_CACHE.put(enterpriseName, ent);
 				}
 			});
 }

 /** Stateless variant of {@link #getISystemToken(Mutiny.StatelessSession, String, IEnterprise)} (same per-enterprise cache). */
 static Uni<UUID> getISystemToken(Mutiny.StatelessSession session, String systemName, IEnterprise<?, ?> enterprise) {
 	UUID enterpriseId = enterprise.getId();
 	Map<UUID, UUID> enterpriseTokens = SYSTEM_TOKEN_CACHE.computeIfAbsent(systemName, k -> new ConcurrentHashMap<>());
 	UUID cachedToken = enterpriseTokens.get(enterpriseId);
 	if (cachedToken != null) {
 		return Uni.createFrom().item(cachedToken);
 	}
 	ISystemsService<?> systemsService = com.guicedee.client.IGuiceContext.get(ISystemsService.class);
 	return getISystem(session, systemName, enterprise).chain(system ->
 			systemsService.getSecurityIdentityToken(session, system).onItem().invoke(token -> {
 				if (token != null) {
 					enterpriseTokens.put(enterpriseId, token);
 				}
 			}));
 }
}
