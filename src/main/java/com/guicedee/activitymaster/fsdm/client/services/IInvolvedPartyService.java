package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.utils.Pair;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.UUID;


/**
 * Service interface for managing involved parties.
 * Involved parties represent individuals or organizations that participate in various activities or arrangements.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IInvolvedPartyService<J extends IInvolvedPartyService<J>>
{
	/**
	 * The name of the Involved Party system.
	 */
	String InvolvedPartySystemName = "Involved Party System";

	/**
	 * Gets a new, uninitialized involved party instance.
	 *
	 * @return A new involved party instance
	 */
	IInvolvedParty<?,?> get();

	/** Stateless variant of {@link #findByID(Mutiny.StatelessSession, UUID)}. */
	Uni<IInvolvedParty<?,?>> findByID(Mutiny.StatelessSession session, UUID id);


	// ---- Stateless "fetch ids/scalars + prep" creates (find-or-create + stateless default security) ----

	/** Stateless variant of {@link #createNameType(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	Uni<IInvolvedPartyNameType<?,?>> createNameType(Mutiny.StatelessSession session, String name, String description, ISystems<?,?> system, UUID... identityToken);

	/** Enum-name stateless variant of {@link #createNameType(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	default Uni<IInvolvedPartyNameType<?,?>> createNameType(Mutiny.StatelessSession session, Enum<?> name, String description, ISystems<?,?> system, UUID... identityToken){
		return createNameType(session, name.toString(), description, system, identityToken);
	}

	/** Stateless variant of {@link #createIdentificationType(Mutiny.StatelessSession, ISystems, String, String, UUID...)}. */
	Uni<IInvolvedPartyIdentificationType<?,?>> createIdentificationType(Mutiny.StatelessSession session, ISystems<?,?> system, String name, String description, UUID... identityToken);

	/** Enum-name stateless variant of {@link #createIdentificationType(Mutiny.StatelessSession, ISystems, String, String, UUID...)}. */
	default Uni<IInvolvedPartyIdentificationType<?,?>> createIdentificationType(Mutiny.StatelessSession session, ISystems<?,?> system, Enum<?> name, String description, UUID... identityToken){
		return createIdentificationType(session, system, name.toString(), description, identityToken);
	}

	/** Stateless variant of {@link #createType(Mutiny.StatelessSession, ISystems, String, String, UUID...)}. */
	Uni<IInvolvedPartyType<?,?>> createType(Mutiny.StatelessSession session, ISystems<?,?> system, String name, String description, UUID... identityToken);

	/** Enum-name stateless variant of {@link #createType(Mutiny.StatelessSession, ISystems, String, String, UUID...)}. */
	default Uni<IInvolvedPartyType<?,?>> createType(Mutiny.StatelessSession session, ISystems<?,?> system, Enum<?> name, String description, UUID... identityToken){
		return createType(session, system, name.toString(), description, identityToken);
	}



	/** Stateless prepped variant of {@link #findInvolvedPartyIdentificationType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IInvolvedPartyIdentificationType<?,?>> findInvolvedPartyIdentificationType(Mutiny.StatelessSession session, String idType, ISystems<?,?> system, UUID... identityToken);

	/** Enum-name stateless variant of {@link #findInvolvedPartyIdentificationType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	default Uni<IInvolvedPartyIdentificationType<?,?>> findInvolvedPartyIdentificationType(Mutiny.StatelessSession session, Enum<?> idType, ISystems<?,?> system, UUID... identityToken)
	{
		return findInvolvedPartyIdentificationType(session, idType.toString(), system, identityToken);
	}

	/** Stateless variant of {@link #findByResourceItem(Mutiny.StatelessSession, IResourceItem, String, ISystems, UUID...)}. */
	Uni<IInvolvedParty<?,?>> findByResourceItem(Mutiny.StatelessSession session, IResourceItem<?,?> idType, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Stateless variant of {@link #create(Mutiny.StatelessSession, ISystems, Pair, boolean, UUID...)} — provisions the
	 * involved party (and its organic/non-organic record + the supplied identification-type link) entirely on a
	 * {@link Mutiny.StatelessSession} via {@code session.insert} + the stateless default-security path.
	 */
	Uni<IInvolvedParty<?, ?>> create(Mutiny.StatelessSession session, ISystems<?, ?> system, Pair<String, String> idTypes,
									 boolean isOrganic, UUID... identityToken);

	/**
	 * Stateless variant of {@link #create(Mutiny.StatelessSession, ISystems, UUID, Pair, boolean, UUID...)} (explicit key).
	 */
	Uni<IInvolvedParty<?, ?>> create(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID key, Pair<String, String> idTypes,
									 boolean isOrganic, UUID... identityToken);

	/** Stateless scope-restricted variant of {@link #createScopeRestricted(Mutiny.StatelessSession, ISystems, UUID, Pair, boolean, ISecurityToken, UUID...)}. */
	Uni<IInvolvedParty<?, ?>> createScopeRestricted(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID key,
													Pair<String, String> idTypes, boolean isOrganic,
													ISecurityToken<?, ?> scopeToken, UUID... identityToken);

	/** Stateless prepped variant of {@link #findType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IInvolvedPartyType<?,?>> findType(Mutiny.StatelessSession session, String type, ISystems<?,?> system, UUID... identityToken);

	/** Stateless prepped variant of {@link #findInvolvedPartyNameType(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IInvolvedPartyNameType<?,?>> findInvolvedPartyNameType(Mutiny.StatelessSession session, String nameType, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findByToken(Mutiny.StatelessSession, ISecurityToken, UUID...)}. */
	Uni<IInvolvedParty<?,?>> findByToken(Mutiny.StatelessSession session, ISecurityToken<?,?> token, UUID... identityToken);

	/** Stateless variant of {@link #find(Mutiny.StatelessSession, UUID)}. */
	Uni<IInvolvedParty<?,?>> find(Mutiny.StatelessSession session, UUID uuid);

	/**
	 * Finds an involved party type by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param uuid    The UUID of the type
	 * @return A Uni emitting the found type
	 */
	Uni<IInvolvedPartyType<?,?>> findType(Mutiny.StatelessSession session, UUID uuid);

	/** Stateless variant of {@link #findNameType(Mutiny.StatelessSession, UUID)}. */
	Uni<IInvolvedPartyNameType<?,?>> findNameType(Mutiny.StatelessSession session, UUID uuid);

	/** Stateless variant of {@link #findIdentificationType(Mutiny.StatelessSession, UUID)}. */
	Uni<IInvolvedPartyIdentificationType<?,?>> findIdentificationType(Mutiny.StatelessSession session, UUID uuid);

	/** Stateless variant of {@link #findByUUID(Mutiny.StatelessSession, UUID, ISystems, UUID...)}. */
	Uni<IInvolvedParty<?,?>> findByUUID(Mutiny.StatelessSession session, UUID token, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findAllByIdentificationType(Mutiny.StatelessSession, String, String)}. */
	Uni<List<IRelationshipValue<IInvolvedParty<?,?>, IInvolvedPartyIdentificationType<?,?>, ?>>> findAllByIdentificationType(Mutiny.StatelessSession session, String identificationType, String value);

	/** Stateless variant of {@link #findByRulesClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	Uni<List<IInvolvedParty<?,?>>> findByRulesClassification(Mutiny.StatelessSession session, String classification, String value, ISystems<?,?> system, UUID... identityToken);

	/** Stateless variant of {@link #findByClassification(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	Uni<IInvolvedParty<?,?>> findByClassification(Mutiny.StatelessSession session, String classification, String value, ISystems<?,?> system, UUID... identityToken);
}
