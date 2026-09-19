package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Date;
import java.util.UUID;


/**
 * Service interface for managing security tokens.
 * Security tokens are used for access control and authorization within the system.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface ISecurityTokenService<J extends ISecurityTokenService<J>>
{
	/**
	 * The name of the Security Tokens system.
	 */
	String SecurityTokenSystemName = "Security Tokens System";

	/**
	 * Gets a new, uninitialized security token instance.
	 *
	 * @return A new security token instance
	 */
	ISecurityToken<?, ?> get();

	/**
	 * Grants access from one token to another with effective dates.
	 *
	 * @param session            The Mutiny session to use
	 * @param fromToken          The granting token
	 * @param toToken            The grantee token
	 * @param create             Whether create access is granted
	 * @param update             Whether update access is granted
	 * @param delete             Whether delete access is granted
	 * @param read               Whether read access is granted
	 * @param system             The system managing the tokens
	 * @param originalId         The original ID of the grant
	 * @param effectiveFromDate  The date from which the grant is effective
	 * @param effectiveToDate    The date until which the grant is effective
	 * @return A Uni that completes when the access is granted
	 */
	Uni<Void> grantAccessToToken(Mutiny.StatelessSession session, @NotNull ISecurityToken<?,?> fromToken, @NotNull ISecurityToken<?,?> toToken,
								 boolean create, boolean update, boolean delete, boolean read,
								 ISystems<?,?> system, String originalId,
								 Date effectiveFromDate, Date effectiveToDate);

	/** Stateless variant of {@link #moveToken(Mutiny.StatelessSession, ISecurityToken, ISecurityToken, ISecurityToken, IClassification, String...)}. */
	Uni<Void> moveToken(Mutiny.StatelessSession session, ISecurityToken<?,?> oldParent, ISecurityToken<?,?> newParent,
	                    ISecurityToken<?,?> child, IClassification<?,?> classification, String... identifyingToken);

	// ============================================================================================
	// Stateless security-bootstrap write primitives (token create / grant / link / apply-defaults).
	// These let the Security Token System provision the entire security structure on a stateless
	// session: scalar existence checks (getCount) + session.insert, with prepped reference reads.
	// ============================================================================================

	/** Stateless variant of {@link #grantAccessToToken(Mutiny.StatelessSession, ISecurityToken, ISecurityToken, boolean, boolean, boolean, boolean, ISystems)}. */
	Uni<Void> grantAccessToToken(Mutiny.StatelessSession session, ISecurityToken<?,?> fromToken, ISecurityToken<?,?> toToken,
								 boolean create, boolean update, boolean delete, boolean read, ISystems<?,?> system);

	/** Stateless variant of {@link #create(Mutiny.StatelessSession, String, String, String, ISystems)}. */
	Uni<ISecurityToken<?,?>> create(Mutiny.StatelessSession session, String classificationValue, String name, String description, ISystems<?,?> system);

	/** Stateless variant of {@link #create(Mutiny.StatelessSession, String, String, String, ISystems, ISecurityToken, UUID...)}. */
	Uni<ISecurityToken<?,?>> create(Mutiny.StatelessSession session, String classificationValue, String name, String description, ISystems<?,?> system, ISecurityToken<?,?> parent, UUID... identityToken);

	/** Stateless variant of {@link #link(Mutiny.StatelessSession, ISecurityToken, ISecurityToken, IClassification, String...)}. */
	Uni<Void> link(Mutiny.StatelessSession session, ISecurityToken<?,?> parent, ISecurityToken<?,?> child, IClassification<?,?> classification, String... identifyingToken);

	/** Stateless variant of {@link #applyDefaultSecurityToTable(Mutiny.StatelessSession, IWarehouseCoreTable, ISystems, UUID...)} — projects row ids (no entity hydration). */
	Uni<Void> applyDefaultSecurityToTable(Mutiny.StatelessSession session, IWarehouseCoreTable<?,?,?,?> table, ISystems<?,?> system, UUID... identityToken);


	/**
	 * Stateless batch variant of {@link #applyScopeRestrictedSecurity(Mutiny.StatelessSession, java.util.Map, ISystems, UUID...)}.
	 * Writes the scope-restricted matrix for every {@code (record → scopeToken)} pair directly on the supplied
	 * {@link Mutiny.StatelessSession} (no nested transaction). Resolve the seven group/folder tokens and the
	 * active flag once, then secure each record sequentially. Partition records across independent stateless
	 * sessions for parallelism.
	 */
	Uni<Void> applyScopeRestrictedSecurity(Mutiny.StatelessSession session,
	                                       java.util.Map<? extends IWarehouseCoreTable<?,?,?,?>, ? extends ISecurityToken<?,?>> recordScopes,
	                                       ISystems<?,?> system, UUID... identityToken);

	// ---------------------------------------------------------------------------------------------
	// Stateless "fetch ids/scalars + prep" folder/group resolvers. SecurityToken has no eager
	// @ManyToOne associations (its concept FK is LAZY) and is not @Cacheable, but to stay consistent
	// and avoid stateless lazy-proxy pitfalls these project the token's OWN scalar columns
	// (id, securityToken, name, description) through the same folder/name/enterprise filters and prep
	// a fresh DETACHED SecurityToken. These return exactly the pre-resolved tokens that the stateless
	// default-security insert API (createDefaultSecurity(Mutiny.StatelessSession, …, groupFolderTokens,
	// …)) consumes, so the canonical seven-grant matrix can be resolved + written on one stateless unit.
	// ---------------------------------------------------------------------------------------------

	/** Stateless prepped variant of {@link #getEveryoneGroup(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getEveryoneGroup(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getEverywhereGroup(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getEverywhereGroup(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getGuestsFolder(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getGuestsFolder(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getRegisteredGuestsFolder(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getRegisteredGuestsFolder(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getVisitorsGuestsFolder(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getVisitorsGuestsFolder(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getAdministratorsFolder(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getAdministratorsFolder(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getSystemsFolder(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getSystemsFolder(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getPluginsFolder(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getPluginsFolder(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #getApplicationsFolder(Mutiny.StatelessSession, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getApplicationsFolder(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Resolves the canonical seven group/folder tokens on a {@link Mutiny.StatelessSession} and returns
	 * them keyed by the {@code IWarehouseCoreTable.SECURITY_*} constants — ready to hand straight to
	 * {@link IWarehouseCoreTable#createDefaultSecurity(Mutiny.StatelessSession, ISystems,
	 * com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise,
	 * com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag,
	 * java.util.Map, UUID...)}.
	 * <p>
	 * This is the previously-missing stateless half of the default-security flow: with it, both the token
	 * resolution AND the per-row inserts run on a stateless session, so an entity's full default-security
	 * matrix can be provisioned end-to-end without a managed persistence context. Each token is resolved
	 * via the stateless prepped folder getters (scalar projection — no eager-association hydration).
	 *
	 * @param session        The stateless session to use
	 * @param system         The system the tokens belong to
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the seven canonical tokens keyed by the {@code SECURITY_*} constants
	 */
	default Uni<java.util.Map<String, ISecurityToken<?,?>>> resolveDefaultGroupFolderTokens(
			Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken) {
		java.util.Map<String, ISecurityToken<?,?>> tokens = new java.util.LinkedHashMap<>();
		return getAdministratorsFolder(session, system, identityToken)
				.invoke(t -> tokens.put(IWarehouseCoreTable.SECURITY_ADMINISTRATORS, t))
				.chain(() -> getEveryoneGroup(session, system, identityToken)
						.invoke(t -> tokens.put(IWarehouseCoreTable.SECURITY_EVERYONE, t)))
				.chain(() -> getEverywhereGroup(session, system, identityToken)
						.invoke(t -> tokens.put(IWarehouseCoreTable.SECURITY_EVERYWHERE, t)))
				.chain(() -> getSystemsFolder(session, system, identityToken)
						.invoke(t -> tokens.put(IWarehouseCoreTable.SECURITY_SYSTEMS, t)))
				.chain(() -> getApplicationsFolder(session, system, identityToken)
						.invoke(t -> tokens.put(IWarehouseCoreTable.SECURITY_APPLICATIONS, t)))
				.chain(() -> getPluginsFolder(session, system, identityToken)
						.invoke(t -> tokens.put(IWarehouseCoreTable.SECURITY_PLUGINS, t)))
				.chain(() -> getGuestsFolder(session, system, identityToken)
						.invoke(t -> tokens.put(IWarehouseCoreTable.SECURITY_GUESTS, t)))
				.replaceWith(tokens);
	}


	/** Stateless variant of {@link #getSecurityToken(Mutiny.StatelessSession, UUID, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getSecurityToken(Mutiny.StatelessSession session, UUID identifyingToken, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped-read variant of {@link #getSecurityTokenByName(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<ISecurityToken<?,?>> getSecurityTokenByName(Mutiny.StatelessSession session, String name, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #applyDefaultSecurityToRows(Mutiny.StatelessSession, java.util.Collection, ISystems, UUID...)}. */
	Uni<Void> applyDefaultSecurityToRows(Mutiny.StatelessSession session, java.util.Collection<? extends IWarehouseCoreTable<?,?,?,?>> rows,
	                                     ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets a security token by its identifying UUID, with an option to override the active flag.
	 *
	 * @param session            The Mutiny session to use
	 * @param identifyingToken   The UUID of the security token to find
	 * @param overrideActiveFlag Whether to include inactive tokens
	 * @param system             The system searching for the token
	 * @param identityToken      Optional security identity tokens for authorization
	 * @return A Uni emitting the found security token
	 */
	Uni<ISecurityToken<?,?>> getSecurityToken(Mutiny.StatelessSession session, UUID identifyingToken, boolean overrideActiveFlag, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #getApplicableSecurityTokenIds(Mutiny.StatelessSession, ISystems, UUID...)}. */
	default Uni<java.util.Set<UUID>> getApplicableSecurityTokenIds(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identityToken)
	{
		if (identityToken == null || identityToken.length == 0)
		{
			return Uni.createFrom().item(java.util.Collections.emptySet());
		}

		var enterprise = system.getEnterprise();
		java.util.List<String> tokens = java.util.Arrays.stream(identityToken)
		                                                 .filter(java.util.Objects::nonNull)
		                                                 .map(UUID::toString)
		                                                 .toList();
		if (tokens.isEmpty())
		{
			return Uni.createFrom().item(java.util.Collections.emptySet());
		}

		java.time.OffsetDateTime now = com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD
				.convertToUTCDateTime(com.entityassist.RootEntity.getNow());

		IActiveFlagService<?> afService = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
		return afService.getVisibleRangeAndUpIds(session, enterprise)
		                .flatMap(visibleIds -> {
			                String sql = "with recursive applicable(securitytokenid) as ( " +
			                             "    select st.securitytokenid " +
			                             "    from security.securitytoken st " +
			                             "    where st.securitytoken in (:tokens) " +
			                             "      and st.enterpriseid = :ent " +
			                             "  union " +
			                             "    select x.parentsecuritytokenid " +
			                             "    from security.securitytokenxsecuritytoken x " +
			                             "    join applicable a on x.childsecuritytokenid = a.securitytokenid " +
			                             "    where x.enterpriseid = :ent " +
			                             "      and (x.effectivefromdate <= :now) " +
			                             "      and (x.effectivetodate > :now) " +
			                             "      and x.activeflagid in (:visibleIds) " +
			                             ") " +
			                             "select securitytokenid from applicable";
			                return session.createNativeQuery(sql, UUID.class)
			                              .setParameter("tokens", tokens)
			                              .setParameter("ent", enterprise.getId())
			                              .setParameter("now", now)
			                              .setParameter("visibleIds", visibleIds)
			                              .getResultList()
			                              .map(ids -> (java.util.Set<UUID>) new java.util.LinkedHashSet<>(ids));
		                });
	}
}
