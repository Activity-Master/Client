package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseSecurityTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Map;
import java.util.UUID;


/**
 * Core warehouse table interface that includes support for row-level security.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 * @param <I> The identifier type
 * @param <S> The security table type
 */
public interface IWarehouseCoreTable<
		J extends IWarehouseCoreTable<J, Q, I,S>,
		Q extends IQueryBuilderDefault<Q, J, I>,
		I extends UUID,
		S extends IWarehouseSecurityTable<S,?,?>
		>
		extends IWarehouseBaseTable<J,Q,I>
{
	/** Canonical key for the Administrators folder token (full access). */
	String SECURITY_ADMINISTRATORS = "administrators";
	/** Canonical key for the Everyone group token (no access). */
	String SECURITY_EVERYONE = "everyone";
	/** Canonical key for the Everywhere group token (read-only). */
	String SECURITY_EVERYWHERE = "everywhere";
	/** Canonical key for the Systems folder token (create/update/read). */
	String SECURITY_SYSTEMS = "systems";
	/** Canonical key for the Applications folder token (create/update/read). */
	String SECURITY_APPLICATIONS = "applications";
	/** Canonical key for the Plugins folder token (create/update/read). */
	String SECURITY_PLUGINS = "plugins";
	/** Canonical key for the Guests folder token (read-only). */
	String SECURITY_GUESTS = "guests";

	/**
	 * Creates a default security record for this entity in the specified system.
	 *
	 * @param session       The reactive session
	 * @param system        The system the entity belongs to
	 * @param identityToken Security tokens for the owner
	 * @return A Uni that completes when the security record is created
	 */
	Uni<Void> createDefaultSecurity(org.hibernate.reactive.mutiny.Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Batch, <strong>stateless-session</strong> variant of default-security creation, intended for the
	 * exhaustive insert paths in installs where potentially millions of security rows are produced.
	 * <p>
	 * Because the group/folder tokens, owning system, enterprise and active flag are shared across
	 * every record, they are resolved <em>once</em> by the caller (on a normal session) and handed in
	 * here, so this method performs pure inserts on a {@link Mutiny.StatelessSession}. A stateless
	 * session keeps no first-level cache and performs no dirty-checking, so the persistence context
	 * does not grow as the number of inserted rows climbs — and the inserts can be JDBC-batched.
	 * <p>
	 * The standard access policy is applied per token:
	 * <ul>
	 *     <li>{@link #SECURITY_ADMINISTRATORS}: create/update/delete/read</li>
	 *     <li>{@link #SECURITY_EVERYONE}: none</li>
	 *     <li>{@link #SECURITY_EVERYWHERE}: read</li>
	 *     <li>{@link #SECURITY_SYSTEMS} / {@link #SECURITY_APPLICATIONS} / {@link #SECURITY_PLUGINS}: create/update/read</li>
	 *     <li>{@link #SECURITY_GUESTS}: read</li>
	 * </ul>
	 * Tokens absent from the map are skipped, so callers may apply a subset.
	 *
	 * @param session           The reactive <strong>stateless</strong> session used for the inserts
	 * @param system            The owning system (referenced by id)
	 * @param enterprise        The owning enterprise (referenced by id)
	 * @param activeFlag        The active flag to stamp on each security row
	 * @param groupFolderTokens The pre-resolved group/folder tokens keyed by the {@code SECURITY_*} constants
	 * @param identityToken     Optional security identity tokens
	 * @return A Uni emitting the number of security rows inserted for this entity
	 */
	Uni<Long> createDefaultSecurity(Mutiny.StatelessSession session, ISystems<?,?> system, IEnterprise<?,?> enterprise,
	                                IActiveFlag<?,?> activeFlag, Map<String, ISecurityToken<?,?>> groupFolderTokens, UUID... identityToken);

	/**
	 * Writes a <strong>single</strong> security grant row for this entity, pairing it with an
	 * <em>arbitrary</em> security token and explicit {create, update, delete, read} flags, on a
	 * {@link Mutiny.StatelessSession}. This is the building block for <em>scoped</em> or custom grants
	 * that fall outside the canonical seven-token default matrix — e.g. granting a geography
	 * <em>scope</em> token (or a bespoke user-group token) read on a record so that only identity tokens
	 * under that scope/branch may see it.
	 * <p>
	 * Like {@link #createDefaultSecurity(Mutiny.StatelessSession, ISystems, IEnterprise, IActiveFlag, Map, UUID...)},
	 * all shared references are supplied pre-resolved so this performs a pure insert; the owning entity
	 * must already be persisted (its id is the security row's back-reference FK). A {@code null}
	 * {@code token} is a no-op (returns {@code 0}).
	 *
	 * @param session       The reactive <strong>stateless</strong> session used for the insert
	 * @param system        The owning system (referenced by id)
	 * @param enterprise    The owning enterprise (referenced by id)
	 * @param activeFlag    The active flag to stamp on the security row
	 * @param token         The security token to grant on this record
	 * @param create        Whether create access is granted
	 * @param update        Whether update access is granted
	 * @param delete        Whether delete access is granted
	 * @param read          Whether read access is granted
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the number of rows inserted (0 or 1)
	 */
	Uni<Long> createSecurityGrant(Mutiny.StatelessSession session, ISystems<?,?> system, IEnterprise<?,?> enterprise,
	                              IActiveFlag<?,?> activeFlag, ISecurityToken<?,?> token,
	                              boolean create, boolean update, boolean delete, boolean read, UUID... identityToken);

	/**
	 * Writes a <strong>scope-restricted</strong> default-security fan-out for this record on a
	 * {@link Mutiny.StatelessSession}: the canonical {@link #SECURITY_ADMINISTRATORS}/{@link #SECURITY_SYSTEMS}/
	 * {@link #SECURITY_APPLICATIONS}/{@link #SECURITY_PLUGINS} grants are written, the
	 * {@link #SECURITY_EVERYONE}/{@link #SECURITY_EVERYWHERE}/{@link #SECURITY_GUESTS} grants are
	 * <strong>omitted</strong> (so the record is <em>not</em> world-readable), and a single
	 * <em>read</em> grant is added for the supplied {@code scopeToken}.
	 * <p>
	 * This is the opt-in counterpart of the public default matrix
	 * ({@link #createDefaultSecurity(Mutiny.StatelessSession, ISystems, IEnterprise, IActiveFlag, Map, UUID...)}):
	 * use it for record types that must be <strong>location/branch restricted</strong> rather than public.
	 * Because the applicable-token climb is <em>child &rarr; parent</em>, a record scoped to token {@code T}
	 * is readable by any identity token located at {@code T} or <strong>below</strong> it (whose ancestors
	 * include {@code T}); identities shallower than {@code T} cannot read it, and identities in unrelated
	 * branches cannot read it at all — that is the restriction.
	 * <p>
	 * The mechanism is intentionally generic (declared on the base table), so it applies uniformly across
	 * <strong>every</strong> warehouse entity type that opts in.
	 *
	 * @param session           The reactive <strong>stateless</strong> session used for the inserts
	 * @param system            The owning system (referenced by id)
	 * @param enterprise        The owning enterprise (referenced by id)
	 * @param activeFlag        The active flag to stamp on each security row
	 * @param groupFolderTokens The pre-resolved group/folder tokens keyed by the {@code SECURITY_*} constants
	 * @param scopeToken        The scope token granted <em>read</em> on this record (e.g. a geography scope token)
	 * @param identityToken     Optional security identity tokens
	 * @return A Uni emitting the number of security rows inserted for this entity
	 */
	Uni<Long> createScopeRestrictedSecurity(Mutiny.StatelessSession session, ISystems<?,?> system, IEnterprise<?,?> enterprise,
	                                        IActiveFlag<?,?> activeFlag, Map<String, ISecurityToken<?,?>> groupFolderTokens,
	                                        ISecurityToken<?,?> scopeToken, UUID... identityToken);

	/**
	 * <strong>Live-session</strong> overload of the scope-restricted matrix, for a single just-created
	 * record on the caller's session (the stateless batch variant cannot see an uncommitted row). Writes
	 * Administrators=CRUD, Systems/Applications/Plugins=create/update/read, <strong>no</strong>
	 * Everyone/Everywhere/Guests grants (→ not world-readable), and {@code scopeToken}=read. Find-or-create
	 * per grant row, so it is idempotent.
	 *
	 * @param session    The reactive (live) session
	 * @param system     The system the entity belongs to
	 * @param scopeToken The scope token granted <em>read</em> on this record
	 * @param identity   Optional security identity tokens
	 * @return A Uni that completes when the restricted security rows exist
	 */
	Uni<Void> createScopeRestrictedSecurity(org.hibernate.reactive.mutiny.Mutiny.Session session, ISystems<?,?> system,
	                                        ISecurityToken<?,?> scopeToken, UUID... identity);

	/**
	 * Counts the in-date-range security rows currently linked to this entity. Primarily a verification
	 * hook for the batch security-creation paths.
	 *
	 * @param session The reactive session
	 * @return A Uni emitting the number of linked security rows
	 */
	Uni<Long> countDefaultSecurity(org.hibernate.reactive.mutiny.Mutiny.Session session);

	/**
	 * Row-level <strong>read</strong> check. Resolves the caller's applicable security-token ids (the
	 * supplied identity tokens plus every group/folder they are a member of, transitively, via
	 * {@link com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService#getApplicableSecurityTokenIds})
	 * and returns {@code true} when this entity carries an in-date-range security row that links one of
	 * those tokens with {@code ReadAllowed = true}.
	 *
	 * @param session       The reactive session
	 * @param system        The system context (provides the enterprise for token expansion)
	 * @param identityToken The caller's security identity token(s)
	 * @return A Uni emitting {@code true} when read access is granted
	 */
	Uni<Boolean> canRead(org.hibernate.reactive.mutiny.Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Row-level <strong>write</strong> check — write is granted when any applicable security token has
	 * {@code CreateAllowed = true} or {@code UpdateAllowed = true} on an in-date-range row of this entity.
	 * See {@link #canRead(Mutiny.Session, ISystems, UUID...)} for how the applicable token set is resolved.
	 *
	 * @param session       The reactive session
	 * @param system        The system context (provides the enterprise for token expansion)
	 * @param identityToken The caller's security identity token(s)
	 * @return A Uni emitting {@code true} when write (create or update) access is granted
	 */
	Uni<Boolean> canWrite(org.hibernate.reactive.mutiny.Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Resolves the set of <strong>this entity type's</strong> ids that the caller may
	 * <strong>read</strong>, for use as a query-level security trim on list queries.
	 * <p>
	 * The caller's identity tokens are first expanded into the full applicable-token set (token plus
	 * every group/folder it belongs to, transitively) via
	 * {@link com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService#getApplicableSecurityTokenIds}.
	 * Every in-date-range security row of this entity's security table whose {@code SecurityTokenID} is
	 * in that set <em>and</em> whose {@code ReadAllowed = true} contributes its owning entity id to the
	 * result. The returned ids are then applied to a query builder with
	 * {@link com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault#canRead(java.util.Collection)}
	 * so {@code getAll()} is automatically security-trimmed.
	 *
	 * @param session       The reactive session
	 * @param system        The system context (provides the enterprise for token expansion)
	 * @param identityToken The caller's security identity token(s)
	 * @return A Uni emitting the readable entity ids; never {@code null}, empty when nothing is readable
	 */
	Uni<java.util.Set<UUID>> readableIds(org.hibernate.reactive.mutiny.Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

}
