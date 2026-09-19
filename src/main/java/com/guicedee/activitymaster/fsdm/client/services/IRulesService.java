package com.guicedee.activitymaster.fsdm.client.services;


import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products.IProduct;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRules;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.UUID;


/**
 * Service interface for managing rules and rules types.
 * Rules define business logic or constraints within the system.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IRulesService<J extends IRulesService<J>>
{
	/**
	 * The name of the Rules system.
	 */
	String RulesSystemName = "Rules System";

	/** Stateless variant of {@link #find(Mutiny.StatelessSession, UUID)}. */
	Uni<IRules<?,?>> find(Mutiny.StatelessSession session, UUID identity);

	/** Stateless variant of {@link #findType(Mutiny.StatelessSession, UUID)}. */
	Uni<IRulesType<?,?>> findType(Mutiny.StatelessSession session, UUID identity);

	/** Stateless variant of {@link #findRules(Mutiny.StatelessSession, String, IEnterprise, UUID...)}. */
	Uni<IRules<?,?>> findRules(Mutiny.StatelessSession session, String name, IEnterprise<?,?> enterprise, UUID... identityToken);

	/** Stateless variant of {@link #findRules(Mutiny.StatelessSession, String, IClassification, IEnterprise, UUID...)}. */
	Uni<IRules<?,?>> findRules(Mutiny.StatelessSession session, String productName, IClassification<?,?> classification, IEnterprise<?,?> enterprise, UUID... identityToken);

	/**
	 * Creates a new rules type using an enum.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The rules type enum
	 * @param system        The system creating the type
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created rules type
	 */
	default Uni<IRulesType<?,?>> createRulesType(Mutiny.StatelessSession session, Enum<?> rulesType, ISystems<?,?> system, UUID... identityToken)
	{
		return createRulesType(session, rulesType.toString(), system, identityToken);
	}

	/** Stateless "fetch ids/scalars + prep" variant of {@link #findRulesTypes(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IRulesType<?,?>> findRulesTypes(Mutiny.StatelessSession session, String rulesType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds rules types by classification value.
	 *
	 * @param session         The Mutiny session to use
	 * @param classifications  The classification name
	 * @param value           The classification value
	 * @param system          The system searching for types
	 * @param identityToken   Optional security identity tokens
	 * @return A Uni emitting a list of found rules types
	 */
	Uni<List<IRulesType<?,?>>> findRulesTypes(Mutiny.StatelessSession session, String classifications, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findByRulesTypes(Mutiny.StatelessSession, IRulesType, String, String, ISystems, UUID...)}. */
	Uni<List<IRules<?,?>>> findByRulesTypes(Mutiny.StatelessSession session, IRulesType<?,?> rulesType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findRuleTypesByRules(Mutiny.StatelessSession, IRules, String, String, ISystems, UUID...)}. */
	Uni<List<IRulesType<?,?>>> findRuleTypesByRules(Mutiny.StatelessSession session, IRules<?,?> rules, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findRuleTypeValuesByRules(Mutiny.StatelessSession, IRules, String, String, ISystems, UUID...)}. */
	Uni<List<IRelationshipValue<IRules<?,?>,IRulesType<?,?>,?>>> findRuleTypeValuesByRules(Mutiny.StatelessSession session, IRules<?,?> rules, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findRulesByProduct(Mutiny.StatelessSession, IProduct, String, String, ISystems, UUID...)}. */
	Uni<List<IRules<?,?>>> findRulesByProduct(Mutiny.StatelessSession session, IProduct<?,?> product, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findRulesByResourceItem(Mutiny.StatelessSession, IResourceItem, String, String, ISystems, UUID...)}. */
	Uni<List<IRelationshipValue<IRules<?,?>, IResourceItem<?,?>,?>>> findRulesByResourceItem(Mutiny.StatelessSession session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);

	// ============================================================================================
	// Stateless create twins (Rules + RulesType, public + scope-restricted). Each runs entirely on the
	// supplied Mutiny.StatelessSession, so independent stateless sessions can provision rules in parallel.
	// ============================================================================================

	/** Stateless variant of {@link #createRules(Mutiny.StatelessSession, String, String, String, ISystems, UUID...)}. */
	Uni<IRules<?,?>> createRules(Mutiny.StatelessSession session, String rulesType, String name, String description, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #createRules(Mutiny.StatelessSession, String, UUID, String, String, ISystems, UUID...)}. */
	Uni<IRules<?,?>> createRules(Mutiny.StatelessSession session, String rulesType, UUID key, String name, String description, ISystems<?,?> system, UUID... identityToken);

	/** Stateless scope-restricted variant of {@link #createRulesScopeRestricted(Mutiny.StatelessSession, String, UUID, String, String, ISystems, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, UUID...)}. */
	Uni<IRules<?,?>> createRulesScopeRestricted(Mutiny.StatelessSession session, String rulesType, UUID key, String name, String description, ISystems<?,?> system,
												com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?,?> scopeToken,
												UUID... identityToken);

	/** Stateless variant of {@link #createRulesType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IRulesType<?,?>> createRulesType(Mutiny.StatelessSession session, String rulesType, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #createRulesType(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	Uni<IRulesType<?,?>> createRulesType(Mutiny.StatelessSession session, String rulesType, String description, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #createRulesType(Mutiny.StatelessSession, String, UUID, String, ISystems, UUID...)}. */
	Uni<IRulesType<?,?>> createRulesType(Mutiny.StatelessSession session, String rulesType, UUID key, String description, ISystems<?,?> system, UUID... identityToken);

	/** Stateless scope-restricted variant of {@link #createRulesTypeScopeRestricted(Mutiny.StatelessSession, String, UUID, String, ISystems, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken, UUID...)}. */
	Uni<IRulesType<?,?>> createRulesTypeScopeRestricted(Mutiny.StatelessSession session, String rulesType, UUID key, String description, ISystems<?,?> system,
													   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?,?> scopeToken,
													   UUID... identityToken);
}
