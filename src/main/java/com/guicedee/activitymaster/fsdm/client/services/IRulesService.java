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

	/**
	 * Creates a new rules instance.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param name          The name of the rules instance
	 * @param description   The description
	 * @param system        The system creating the rules
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created rules
	 */
	Uni<IRules<?, ?>> createRules(Mutiny.Session session, String rulesType, String name, String description, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new rules instance with a specific key.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param key           The UUID key for the rules
	 * @param name          The name of the rules instance
	 * @param description   The description
	 * @param system        The system creating the rules
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created rules
	 */
	Uni<IRules<?, ?>> createRules(Mutiny.Session session, String rulesType, UUID key, String name, String description, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Creates a new rules instance that is <strong>scope-restricted</strong> rather than world-readable. Identical
	 * to {@link #createRules(Mutiny.Session, String, UUID, String, String, ISystems, UUID...)} except the rules are
	 * secured with the restricted matrix: only Administrators / Systems / Applications / Plugins retain access, plus
	 * a <em>read</em> grant for {@code scopeToken}. Because the applicable-token climb is child&rarr;parent, only
	 * identity tokens located at the {@code scopeToken} node <em>or below it</em> may read the rules.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param key           The UUID key for the rules, or {@code null} to generate one
	 * @param name          The name of the rules instance
	 * @param description   The description
	 * @param system        The system creating the rules
	 * @param scopeToken    The scope token granted read on the new rules
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created (scope-restricted) rules
	 */
	Uni<IRules<?, ?>> createRulesScopeRestricted(Mutiny.Session session, String rulesType, UUID key, String name, String description, ISystems<?, ?> system,
												 com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
												 UUID... identityToken);

	/**
	 * Finds a rules instance by its unique ID.
	 *
	 * @param session  The Mutiny session to use
	 * @param identity The UUID of the rules instance
	 * @return A Uni emitting the found rules instance
	 */
	Uni<IRules<?,?>> find(Mutiny.Session session, UUID identity);

	/**
	 * Finds a rules type by its unique ID.
	 *
	 * @param session  The Mutiny session to use
	 * @param identity The UUID of the rules type
	 * @return A Uni emitting the found rules type
	 */
	Uni<IRulesType<?,?>> findType(Mutiny.Session session, UUID identity);

	/**
	 * Finds rules by name within an enterprise.
	 *
	 * @param session       The Mutiny session to use
	 * @param name          The name of the rules
	 * @param enterprise    The enterprise to search within
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the found rules
	 */
	Uni<IRules<?,?>> findRules(Mutiny.Session session, String name, IEnterprise<?,?> enterprise, UUID... identityToken);

	/**
	 * Finds rules by product name, classification, and enterprise.
	 *
	 * @param session        The Mutiny session to use
	 * @param productName   The name of the product
	 * @param classification The classification constraint
	 * @param enterprise     The enterprise to search within
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found rules
	 */
	Uni<IRules<?,?>> findRules(Mutiny.Session session, String productName, IClassification<?,?> classification, IEnterprise<?,?> enterprise, UUID... identityToken);

	/**
	 * Creates a new rules type using an enum.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The rules type enum
	 * @param system        The system creating the type
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created rules type
	 */
	default Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, Enum<?> rulesType, ISystems<?,?> system, UUID... identityToken)
	{
		return createRulesType(session, rulesType.toString(), system, identityToken);
	}

	/**
	 * Creates a new rules type by name.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param system        The system creating the type
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created rules type
	 */
	Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, String rulesType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new rules type by name and description.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param description   The description
	 * @param system        The system creating the type
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created rules type
	 */
	Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, String rulesType, String description, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new rules type with a specific key.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param key           The UUID key for the type
	 * @param description   The description
	 * @param system        The system creating the type
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created rules type
	 */
	Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, String rulesType, UUID key, String description, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Creates a new rules type that is <strong>scope-restricted</strong>. Same as
	 * {@link #createRulesType(Mutiny.Session, String, UUID, String, ISystems, UUID...)} but secured with the
	 * restricted matrix plus a <em>read</em> grant for {@code scopeToken}.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param key           The UUID key for the type, or {@code null} to generate one
	 * @param description   The description
	 * @param system        The system creating the type
	 * @param scopeToken    The scope token granted read on the new rules type
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the created (scope-restricted) rules type
	 */
	Uni<IRulesType<?,?>> createRulesTypeScopeRestricted(Mutiny.Session session, String rulesType, UUID key, String description, ISystems<?, ?> system,
														com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?, ?> scopeToken,
														UUID... identityToken);

	/**
	 * Finds rules types by name.
	 *
	 * @param session       The Mutiny session to use
	 * @param rulesType     The name of the rules type
	 * @param system        The system searching for the type
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the found rules type
	 */
	Uni<IRulesType<?,?>> findRulesTypes(Mutiny.Session session, String rulesType, ISystems<?,?> system, UUID... identityToken);

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
	Uni<List<IRulesType<?,?>>> findRulesTypes(Mutiny.Session session, String classifications, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds rules instances by rules type and classification.
	 *
	 * @param session             The Mutiny session to use
	 * @param rulesType           The rules type
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for rules
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found rules instances
	 */
	Uni<List<IRules<?,?>>> findByRulesTypes(Mutiny.Session session, IRulesType<?,?> rulesType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds rules types associated with specific rules and classification.
	 *
	 * @param session             The Mutiny session to use
	 * @param rules               The rules instance
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for types
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found rules types
	 */
	Uni<List<IRulesType<?,?>>> findRuleTypesByRules(Mutiny.Session session, IRules<?,?> rules, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds relationship values between rules and rules types.
	 *
	 * @param session             The Mutiny session to use
	 * @param rules               The rules instance
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for values
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of relationship values
	 */
	Uni<List<IRelationshipValue<IRules<?,?>,IRulesType<?,?>,?>>> findRuleTypeValuesByRules(Mutiny.Session session, IRules<?,?> rules, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds rules associated with a product and classification.
	 *
	 * @param session             The Mutiny session to use
	 * @param product             The product
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for rules
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found rules instances
	 */
	Uni<List<IRules<?,?>>> findRulesByProduct(Mutiny.Session session, IProduct<?,?> product, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds rules associated with a resource item and classification.
	 *
	 * @param session             The Mutiny session to use
	 * @param resourceItem        The resource item
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for rules
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of relationship values containing rules
	 */
	Uni<List<IRelationshipValue<IRules<?,?>, IResourceItem<?,?>,?>>> findRulesByResourceItem(Mutiny.Session session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);
}
