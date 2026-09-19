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
	Uni<IArrangement<?,?>> create(Mutiny.StatelessSession session, Enum<?> type,
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
	 * Stateless variant of {@link #create(Mutiny.StatelessSession, UUID, String, String, String, ISystems, UUID...)} —
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
	 * Stateless variant of {@link #create(Mutiny.StatelessSession, String, UUID, String, String, ISystems, UUID...)} —
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

	/** Stateless scope-restricted variant of {@link #createScopeRestricted(Mutiny.StatelessSession, String, UUID, String, String, ISystems, ISecurityToken, UUID...)}. */
	Uni<IArrangement<?,?>> createScopeRestricted(Mutiny.StatelessSession session, String type, UUID key,
												 String arrangementTypeClassification,
												 String arrangementTypeValue,
												 ISystems<?, ?> system,
												 ISecurityToken<?, ?> scopeToken,
												 UUID... identityToken);

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
	Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.StatelessSession session, String type, UUID key, ISystems<?, ?> system, UUID... identityToken);

	/** Stateless find-or-insert variant of {@link #createArrangementType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.StatelessSession session, String type, ISystems<?,?> system, UUID... identityToken);

	/** Stateless Enum convenience over {@link #createArrangementType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	default Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.StatelessSession session, Enum<?> type, ISystems<?,?> system, UUID... identityToken)
	{
		return createArrangementType(session, type.toString(), system, identityToken);
	}

	/** Stateless "fetch ids/scalars + prep" variant of {@link #findArrangementType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
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
	Uni<List<IArrangement<?,?>>> findInvolvedPartyArrangements(Mutiny.StatelessSession session, IInvolvedParty<?,?> ip, String arrType, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.StatelessSession session, String arrType, String value, ISystems<?,?> systems, UUID... identityToken);

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
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGTEWithIP(Mutiny.StatelessSession session, String arrangementType, String classificationName,
																		   IInvolvedParty<?,?> withInvolvedParty, String ipClassification, IArrangement<?,?> withParent,
																		   IResourceItem<?,?> resourceItem,
																		   String resourceItemClassification,
																		   String value, ISystems<?,?> system, UUID... identityToken);


	// ---- Stateless finder twins ----

	/** Stateless variant of {@link #find(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IArrangementType<?,?>> find(Mutiny.StatelessSession session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #find(Mutiny.StatelessSession, UUID, ISystems, UUID...)}. */
	Uni<IArrangement<?,?>> find(Mutiny.StatelessSession session, UUID id, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #find(Mutiny.StatelessSession, UUID)}. */
	Uni<IArrangement<?,?>> find(Mutiny.StatelessSession session, UUID id);

	/** Stateless variant of {@link #findAll(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findAll(Mutiny.StatelessSession session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementByResourceItem(Mutiny.StatelessSession, IResourceItem, String, String, ISystems, UUID...)}. */
	Uni<IArrangement<?,?>> findArrangementByResourceItem(Mutiny.StatelessSession session, IResourceItem<?,?> resourceItem, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, ISystems, UUID...)}. */
	Uni<IArrangement<?,?>> findArrangementByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementInvolvedParties(Mutiny.StatelessSession, IArrangement, String, String, ISystems, UUID...)}. */
	Uni<List<IInvolvedParty<?,?>>> findArrangementInvolvedParties(Mutiny.StatelessSession session, IArrangement<?,?> arrangement, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByRulesType(Mutiny.StatelessSession, IRulesType, String, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByRulesType(Mutiny.StatelessSession session, IRulesType<?,?> ruleType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassification(Mutiny.StatelessSession, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationGT(Mutiny.StatelessSession, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGT(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationGTE(Mutiny.StatelessSession, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGTE(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationLT(Mutiny.StatelessSession, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLT(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByClassificationLTE(Mutiny.StatelessSession, String, IArrangement, String, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLTE(Mutiny.StatelessSession session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, LocalDateTime, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findArrangementsByInvolvedParty(Mutiny.StatelessSession, IInvolvedParty, String, String, LocalDateTime, LocalDateTime, ISystems, UUID...)}. */
	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, LocalDateTime endDate, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #completeArrangement(Mutiny.StatelessSession, IArrangement, ISystems, UUID...)}. */
	Uni<@NotNull IArrangement<?,?>> completeArrangement(Mutiny.StatelessSession session, IArrangement<?,?> arrangement, ISystems<?,?> system, UUID... identityToken);

}
