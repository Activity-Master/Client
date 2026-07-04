package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangementType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Service interface for managing arrangements.
 * Arrangements represent agreements or contracts between parties, associated with systems and classifications.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IArrangementsService<J extends IArrangementsService<J>>
{
	/**
	 * The name of the Arrangements system.
	 */
	String ArrangementSystemName = "Arrangements System";


	/**
	 * Creates a new arrangement using enumeration types.
	 *
	 * @param session                        The Mutiny session to use
	 * @param type                           The arrangement type
	 * @param arrangementTypeClassification  The classification for the arrangement type
	 * @param arrangementTypeValue           The value for the arrangement type classification
	 * @param system                         The system creating the arrangement
	 * @param identityToken                  Optional security identity tokens
	 * @return A Uni emitting the created arrangement
	 */
	default
	Uni<IArrangement<?,?>> create(Mutiny.Session session, Enum<?> type,
								  Enum<?> arrangementTypeClassification,
								  String arrangementTypeValue,
								  ISystems<?,?> system,
								  UUID... identityToken)
	{
		return create(session, null,type.toString(), arrangementTypeClassification.toString(), arrangementTypeValue, system, identityToken);
	}

	/**
	 * Gets a new, uninitialized arrangement instance.
	 *
	 * @return A new arrangement instance
	 */
	IArrangement<?,?> get();

	/**
	 * Creates a new arrangement with a specific key.
	 *
	 * @param session                        The Mutiny session to use
	 * @param key                            The UUID key for the arrangement
	 * @param type                           The arrangement type
	 * @param arrangementTypeClassification  The classification for the arrangement type
	 * @param arrangementTypeValue           The value for the arrangement type classification
	 * @param system                         The system creating the arrangement
	 * @param identityToken                  Optional security identity tokens
	 * @return A Uni emitting the created arrangement
	 */
	Uni<IArrangement<?,?>> create(Mutiny.Session session,UUID key, String type,
                                  String arrangementTypeClassification,
                                  String arrangementTypeValue,
                                  ISystems<?,?> system,
                                  UUID... identityToken);

	/**
	 * Creates a new arrangement type.
	 *
	 * @param session        The Mutiny session to use
	 * @param type           The arrangement type enum
	 * @param system         The system creating the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created arrangement type
	 */
	default Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.Session session, Enum<?> type, ISystems<?,?> system, UUID... identityToken)
	{
		return createArrangementType(session, type.toString(), system, identityToken);
	}

	/**
	 * Creates a new arrangement with a specific type and key.
	 *
	 * @param session                        The Mutiny session to use
	 * @param type                           The arrangement type
	 * @param key                            The UUID key for the arrangement
	 * @param arrangementTypeClassification  The classification for the arrangement type
	 * @param arrangementTypeValue           The value for the arrangement type classification
	 * @param system                         The system creating the arrangement
	 * @param identityToken                  Optional security identity tokens
	 * @return A Uni emitting the created arrangement
	 */
	Uni<IArrangement<?,?>> create(Mutiny.Session session, String type, UUID key,
								  String arrangementTypeClassification,
								  String arrangementTypeValue,
								  ISystems<?, ?> system,
								  UUID... identityToken);

	/**
	 * Stateless variant of {@link #create(Mutiny.Session, UUID, String, String, String, ISystems, UUID...)} —
	 * provisions the arrangement (and its arrangement-type link) entirely on a {@link Mutiny.StatelessSession}
	 * via {@code session.insert} + the stateless default-security path. World-readable (public) security matrix.
	 *
	 * @param session                        The stateless session to use
	 * @param key                            The UUID key for the arrangement, or {@code null} to generate one
	 * @param type                           The arrangement type
	 * @param arrangementTypeClassification  The classification for the arrangement type
	 * @param arrangementTypeValue           The value for the arrangement type classification
	 * @param system                         The system creating the arrangement
	 * @param identityToken                  Optional security identity tokens
	 * @return A Uni emitting the created arrangement
	 */
	Uni<IArrangement<?,?>> create(Mutiny.StatelessSession session, UUID key, String type,
								  String arrangementTypeClassification,
								  String arrangementTypeValue,
								  ISystems<?, ?> system,
								  UUID... identityToken);

	/**
	 * Stateless variant of {@link #create(Mutiny.Session, String, UUID, String, String, ISystems, UUID...)} —
	 * provisions the arrangement (and its arrangement-type link) entirely on a {@link Mutiny.StatelessSession}.
	 *
	 * @param session                        The stateless session to use
	 * @param type                           The arrangement type
	 * @param key                            The UUID key for the arrangement, or {@code null} to generate one
	 * @param arrangementTypeClassification  The classification for the arrangement type
	 * @param arrangementTypeValue           The value for the arrangement type classification
	 * @param system                         The system creating the arrangement
	 * @param identityToken                  Optional security identity tokens
	 * @return A Uni emitting the created arrangement
	 */
	Uni<IArrangement<?,?>> create(Mutiny.StatelessSession session, String type, UUID key,
								  String arrangementTypeClassification,
								  String arrangementTypeValue,
								  ISystems<?, ?> system,
								  UUID... identityToken);

	/**
	 * Creates a new arrangement that is <strong>scope-restricted</strong> rather than world-readable.
	 * Identical to {@link #create(Mutiny.Session, String, UUID, String, String, ISystems, UUID...)} except
	 * the arrangement is secured with the restricted matrix: only Administrators / Systems / Applications /
	 * Plugins retain access, plus a <em>read</em> grant for {@code scopeToken}. Because the applicable-token
	 * climb is child&rarr;parent, only identity tokens located at the {@code scopeToken} node <em>or below
	 * it</em> may read the arrangement.
	 *
	 * @param session                        The Mutiny session to use
	 * @param type                           The arrangement type
	 * @param key                            The UUID key for the arrangement, or {@code null} to generate one
	 * @param arrangementTypeClassification  The classification for the arrangement type
	 * @param arrangementTypeValue           The value for the arrangement type classification
	 * @param system                         The system creating the arrangement
	 * @param scopeToken                     The scope token granted read on the new arrangement
	 * @param identityToken                  Optional security identity tokens
	 * @return A Uni emitting the created (scope-restricted) arrangement
	 */
	Uni<IArrangement<?,?>> createScopeRestricted(Mutiny.Session session, String type, UUID key,
												 String arrangementTypeClassification,
												 String arrangementTypeValue,
												 ISystems<?, ?> system,
												 ISecurityToken<?, ?> scopeToken,
												 UUID... identityToken);

	/** Stateless scope-restricted variant of {@link #createScopeRestricted(Mutiny.Session, String, UUID, String, String, ISystems, ISecurityToken, UUID...)}. */
	Uni<IArrangement<?,?>> createScopeRestricted(Mutiny.StatelessSession session, String type, UUID key,
												 String arrangementTypeClassification,
												 String arrangementTypeValue,
												 ISystems<?, ?> system,
												 ISecurityToken<?, ?> scopeToken,
												 UUID... identityToken);

	/**
	 * Creates a new arrangement type by name.
	 *
	 * @param session        The Mutiny session to use
	 * @param type           The name of the arrangement type
	 * @param system         The system creating the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created arrangement type
	 */
	Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new arrangement type with a specific key.
	 *
	 * @param session        The Mutiny session to use
	 * @param type           The name of the arrangement type
	 * @param key            The UUID key for the arrangement type
	 * @param system         The system creating the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created arrangement type
	 */
	Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.Session session, String type, UUID key, ISystems<?, ?> system, UUID... identityToken);

	/** Stateless find-or-insert variant of {@link #createArrangementType(Mutiny.Session, String, ISystems, UUID...)}. */
	Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.StatelessSession session, String type, ISystems<?,?> system, UUID... identityToken);

	/** Stateless Enum convenience over {@link #createArrangementType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	default Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.StatelessSession session, Enum<?> type, ISystems<?,?> system, UUID... identityToken)
	{
		return createArrangementType(session, type.toString(), system, identityToken);
	}

	/**
	 * Finds an arrangement type by name.
	 *
	 * @param session        The Mutiny session to use
	 * @param type           The name of the arrangement type
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found arrangement type
	 */
	Uni<IArrangementType<?, ?>> findArrangementType(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);

	/** Stateless "fetch ids/scalars + prep" variant of {@link #findArrangementType(Mutiny.Session, String, ISystems, UUID...)}. */
	Uni<IArrangementType<?, ?>> findArrangementType(Mutiny.StatelessSession session, String type, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds arrangements associated with an involved party.
	 *
	 * @param session        The Mutiny session to use
	 * @param ip             The involved party
	 * @param arrType        The arrangement type
	 * @param systems        The system searching for arrangements
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findInvolvedPartyArrangements(Mutiny.Session session, IInvolvedParty<?,?> ip, String arrType, ISystems<?,?> systems, UUID... identityToken);

	/**
	 * Finds arrangements by classification value.
	 *
	 * @param session        The Mutiny session to use
	 * @param arrType        The arrangement type
	 * @param value          The classification value
	 * @param systems        The system searching for arrangements
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.Session session, String arrType, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassification(Mutiny.Session, String, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.StatelessSession session, String arrType, String value, ISystems<?,?> systems, UUID... identityToken);

	/**
	 * Finds arrangements by classification value greater than the specified value.
	 *
	 * @param session        The Mutiny session to use
	 * @param arrType        The arrangement type
	 * @param withParent     The parent arrangement constraint
	 * @param value          The classification value
	 * @param systems        The system searching for arrangements
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGT(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/**
	 * Finds arrangements by classification value greater than or equal to the specified value.
	 *
	 * @param session        The Mutiny session to use
	 * @param arrType        The arrangement type
	 * @param withParent     The parent arrangement constraint
	 * @param value          The classification value
	 * @param systems        The system searching for arrangements
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGTE(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/**
	 * Complex search for arrangements by classification, involved party, and resource item.
	 *
	 * @param session                    The Mutiny session to use
	 * @param arrangementType           The arrangement type
	 * @param classificationName        The name of the classification
	 * @param withInvolvedParty         The involved party constraint
	 * @param ipClassification          The classification for the involved party
	 * @param withParent                The parent arrangement constraint
	 * @param resourceItem              The resource item constraint
	 * @param resourceItemClassification The classification for the resource item
	 * @param value                     The classification value
	 * @param system                    The system searching for arrangements
	 * @param identityToken             Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGTEWithIP(Mutiny.Session session, String arrangementType, String classificationName,
																		   IInvolvedParty<?,?> withInvolvedParty, String ipClassification, IArrangement<?,?> withParent,
																		   IResourceItem<?,?> resourceItem,
																		   String resourceItemClassification,
																		   String value, ISystems<?,?> system, UUID... identityToken);


	/**
	 * Finds arrangements by classification value less than the specified value.
	 *
	 * @param session        The Mutiny session to use
	 * @param arrType        The arrangement type
	 * @param withParent     The parent arrangement constraint
	 * @param value          The classification value
	 * @param systems        The system searching for arrangements
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLT(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/**
	 * Finds arrangements by classification value less than or equal to the specified value.
	 *
	 * @param session        The Mutiny session to use
	 * @param arrType        The arrangement type
	 * @param withParent     The parent arrangement constraint
	 * @param value          The classification value
	 * @param systems        The system searching for arrangements
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLTE(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/**
	 * Finds arrangements by classification and parent arrangement.
	 *
	 * @param session        The Mutiny session to use
	 * @param arrType        The arrangement type
	 * @param withParent     The parent arrangement constraint
	 * @param value          The classification value
	 * @param systems        The system searching for arrangements
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/**
	 * Finds an arrangement associated with a specific resource item.
	 *
	 * @param session             The Mutiny session to use
	 * @param resourceItem        The resource item
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for the arrangement
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting the found arrangement
	 */
	Uni<IArrangement<?,?>> findArrangementByResourceItem(Mutiny.Session session, IResourceItem<?,?> resourceItem, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds an arrangement associated with a specific involved party.
	 *
	 * @param session             The Mutiny session to use
	 * @param involvedParty       The involved party
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for the arrangement
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting the found arrangement
	 */
	Uni<IArrangement<?,?>> findArrangementByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds arrangements associated with a specific rules type.
	 *
	 * @param session             The Mutiny session to use
	 * @param ruleType            The rules type
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for arrangements
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByRulesType(Mutiny.Session session, IRulesType<?,?> ruleType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds arrangements associated with an involved party within a date range starting from {@code startDate}.
	 *
	 * @param session             The Mutiny session to use
	 * @param involvedParty       The involved party
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param startDate           The start date constraint
	 * @param system              The system searching for arrangements
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds arrangements associated with an involved party within a specific date range.
	 *
	 * @param session             The Mutiny session to use
	 * @param involvedParty       The involved party
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param startDate           The start date constraint
	 * @param endDate             The end date constraint
	 * @param system              The system searching for arrangements
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, LocalDateTime endDate, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds all arrangements for a specific involved party and classification.
	 *
	 * @param session             The Mutiny session to use
	 * @param involvedParty       The involved party
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for arrangements
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds involved parties associated with an arrangement.
	 *
	 * @param session             The Mutiny session to use
	 * @param arrangement         The arrangement
	 * @param classificationName  The classification name
	 * @param value               The classification value
	 * @param system              The system searching for involved parties
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found involved parties
	 */
	Uni<List<IInvolvedParty<?,?>>> findArrangementInvolvedParties(Mutiny.Session session, IArrangement<?,?> arrangement, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds an arrangement type by its name.
	 *
	 * @param session             The Mutiny session to use
	 * @param arrangementType     The arrangement type name
	 * @param system              The system searching for the type
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting the found arrangement type
	 */
	Uni<@NotNull IArrangementType<?,?>> find(Mutiny.Session session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds an arrangement by its unique ID.
	 *
	 * @param session             The Mutiny session to use
	 * @param id                  The UUID of the arrangement
	 * @param system              The system searching for the arrangement
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting the found arrangement
	 */
	Uni<@NotNull IArrangement<?,?>> find(Mutiny.Session session, UUID id, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds an arrangement by its unique ID using the provided session.
	 *
	 * @param session  The Mutiny session to use
	 * @param id       The UUID of the arrangement
	 * @return A Uni emitting the found arrangement
	 */
	Uni<@NotNull IArrangement<?,?>> find(Mutiny.Session session, UUID id);

	/**
	 * Finds all arrangements of a specific type.
	 *
	 * @param session             The Mutiny session to use
	 * @param arrangementType     The arrangement type name
	 * @param system              The system searching for arrangements
	 * @param identityToken       Optional security identity tokens
	 * @return A Uni emitting a list of found arrangements
	 */
	Uni<@NotNull List<IArrangement<?,?>>> findAll(Mutiny.Session session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Marks an arrangement as completed.
	 *
	 * @param session        The Mutiny session to use
	 * @param arrangement    The arrangement to complete
	 * @param system         The system performing the operation
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the completed arrangement
	 */
	Uni<@NotNull IArrangement<?,?>> completeArrangement(Mutiny.Session session, IArrangement<?,?> arrangement, ISystems<?,?> system, UUID... identityToken);

	// ---- Stateless finder twins ----

	/** Stateless variant of {@link #find(Mutiny.Session, String, ISystems, UUID...)}. */
	Uni<IArrangementType<?,?>> find(Mutiny.StatelessSession session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #find(Mutiny.Session, UUID, ISystems, UUID...)}. */
	Uni<IArrangement<?,?>> find(Mutiny.StatelessSession session, UUID id, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #find(Mutiny.Session, UUID)}. */
	Uni<IArrangement<?,?>> find(Mutiny.StatelessSession session, UUID id);

	/** Stateless variant of {@link #findAll(Mutiny.Session, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findAll(Mutiny.StatelessSession session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementByResourceItem(Mutiny.Session, IResourceItem, String, String, ISystems, UUID...)}. */
	Uni<IArrangement<?,?>> findArrangementByResourceItem(Mutiny.StatelessSession session, IResourceItem<?,?> resourceItem, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementByInvolvedParty(Mutiny.Session, IInvolvedParty, String, String, ISystems, UUID...)}. */
	Uni<IArrangement<?,?>> findArrangementByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementInvolvedParties(Mutiny.Session, IArrangement, String, String, ISystems, UUID...)}. */
	Uni<List<IInvolvedParty<?,?>>> findArrangementInvolvedParties(Mutiny.StatelessSession session, IArrangement<?,?> arrangement, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByInvolvedParty(Mutiny.Session, IInvolvedParty, String, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByRulesType(Mutiny.Session, IRulesType, String, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByRulesType(Mutiny.StatelessSession session, IRulesType<?,?> ruleType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassification(Mutiny.Session, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationGT(Mutiny.Session, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGT(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationGTE(Mutiny.Session, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGTE(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationLT(Mutiny.Session, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLT(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationLTE(Mutiny.Session, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLTE(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByInvolvedParty(Mutiny.Session, IInvolvedParty, String, String, LocalDateTime, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByInvolvedParty(Mutiny.Session, IInvolvedParty, String, String, LocalDateTime, LocalDateTime, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, LocalDateTime endDate, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #completeArrangement(Mutiny.Session, IArrangement, ISystems, UUID...)}. */
	Uni<@NotNull IArrangement<?,?>> completeArrangement(Mutiny.StatelessSession session, IArrangement<?,?> arrangement, ISystems<?,?> system, UUID... identityToken);

}
