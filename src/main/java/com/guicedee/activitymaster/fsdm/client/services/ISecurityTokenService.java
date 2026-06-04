package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
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
	 * Grants access from one token to another.
	 *
	 * @param session    The Mutiny session to use
	 * @param fromToken  The granting token
	 * @param toToken    The grantee token
	 * @param create     Whether create access is granted
	 * @param update     Whether update access is granted
	 * @param delete     Whether delete access is granted
	 * @param read       Whether read access is granted
	 * @param system     The system managing the tokens
	 * @return A Uni that completes when the access is granted
	 */
	Uni<Void> grantAccessToToken(Mutiny.Session session, ISecurityToken<?,?> fromToken, ISecurityToken<?,?> toToken,
                                 boolean create, boolean update, boolean delete, boolean read, ISystems<?,?> system);

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
	Uni<Void> grantAccessToToken(Mutiny.Session session, @NotNull ISecurityToken<?,?> fromToken, @NotNull ISecurityToken<?,?> toToken,
								 boolean create, boolean update, boolean delete, boolean read,
								 ISystems<?,?> system, String originalId,
								 Date effectiveFromDate, Date effectiveToDate);

	/**
	 * Creates a new security token.
	 *
	 * @param session             The Mutiny session to use
	 * @param classificationValue The classification value for the token
	 * @param name                The name of the token
	 * @param description         The description of the token
	 * @param system              The system creating the token
	 * @return A Uni emitting the created security token
	 */
	Uni<ISecurityToken<?,?>> create(Mutiny.Session session, String classificationValue, String name, String description, ISystems<?,?> system);

	/**
	 * Creates a new security token with a parent and identity tokens.
	 *
	 * @param session             The Mutiny session to use
	 * @param classificationValue The classification value for the token
	 * @param name                The name of the token
	 * @param description         The description of the token
	 * @param system              The system creating the token
	 * @param parent              The parent security token
	 * @param identityToken       The identity tokens for the operation
	 * @return A Uni emitting the created security token
	 */
	Uni<ISecurityToken<?,?>> create(Mutiny.Session session, String classificationValue, String name, String description, ISystems<?,?> system, ISecurityToken<?,?> parent, UUID... identityToken);

	/**
	 * Links a parent token and a child token.
	 *
	 * @param session          The Mutiny session to use
	 * @param parent           The parent token
	 * @param child            The child token
	 * @param classification   The classification for the link
	 * @param identifyingToken The identity tokens for the operation
	 * @return A Uni that completes when the link is created
	 */
	Uni<Void> link(Mutiny.Session session, ISecurityToken<?,?> parent, ISecurityToken<?,?> child, IClassification<?,?> classification, String... identifyingToken);

	/**
	 * Gets the 'Everyone' group token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Everyone' group token
	 */
	Uni<ISecurityToken<?,?>> getEveryoneGroup(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Everywhere' group token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Everywhere' group token
	 */
	Uni<ISecurityToken<?,?>> getEverywhereGroup(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Guests' folder token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Guests' folder token
	 */
	Uni<ISecurityToken<?,?>> getGuestsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Registered Guests' folder token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Registered Guests' folder token
	 */
	Uni<ISecurityToken<?,?>> getRegisteredGuestsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Visitors Guests' folder token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Visitors Guests' folder token
	 */
	Uni<ISecurityToken<?,?>> getVisitorsGuestsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Administrators' folder token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Administrators' folder token
	 */
	Uni<ISecurityToken<?,?>> getAdministratorsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Systems' folder token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Systems' folder token
	 */
	Uni<ISecurityToken<?,?>> getSystemsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Plugins' folder token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Plugins' folder token
	 */
	Uni<ISecurityToken<?,?>> getPluginsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the 'Applications' folder token.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'Applications' folder token
	 */
	Uni<ISecurityToken<?,?>> getApplicationsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets a security token by its identifying UUID.
	 *
	 * @param session           The Mutiny session to use
	 * @param identifyingToken  The UUID of the security token to find
	 * @param system            The system searching for the token
	 * @param identityToken     Optional security identity tokens for authorization
	 * @return A Uni emitting the found security token
	 */
	Uni<ISecurityToken<?,?>> getSecurityToken(Mutiny.Session session, UUID identifyingToken, ISystems<?,?> system, UUID... identityToken);

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
	Uni<ISecurityToken<?,?>> getSecurityToken(Mutiny.Session session, UUID identifyingToken, boolean overrideActiveFlag, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Expands the supplied identity tokens into the complete set of SecurityToken IDs (PKs) that are able to
	 * grant access to a row for the calling party. In other words: the caller's own tokens plus every group
	 * (and group-of-group, transitively) that those tokens are a member of.
	 * <p>
	 * The supplied {@code identityToken} values are the {@code SecurityToken.SecurityToken} (varchar) values
	 * - the same values accepted by {@link #getSecurityToken(Mutiny.Session, UUID, ISystems, UUID...)} - while
	 * the hierarchy (Security.SecurityTokenXSecurityToken) and the per-row grant tables key on the
	 * {@code SecurityTokenID} primary key. This method seeds the search from the token strings and walks the
	 * child &rarr; parent edges to resolve every applicable grantee id.
	 * <p>
	 * The whole expansion is performed in a single PostgreSQL {@code WITH RECURSIVE} query so the group search
	 * costs one database round-trip regardless of hierarchy depth. The resulting id set is what a {@code canRead}
	 * filter should match the row's {@code SecurityTokenID} against (with {@code ReadAllowed = true}).
	 *
	 * @return never {@code null}; an empty set when no identity tokens are supplied.
	 */
	default Uni<java.util.Set<UUID>> getApplicableSecurityTokenIds(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken)
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
			                // Seed: the PKs for the supplied token strings.
			                // Recurse upward: every parent group reachable through an active, in-range membership edge.
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
