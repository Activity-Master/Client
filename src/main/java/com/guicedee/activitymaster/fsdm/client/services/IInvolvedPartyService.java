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

	/**
	 * Finds an involved party by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param id      The UUID of the involved party
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?,?>> findByID(Mutiny.Session session, UUID id);

	/**
	 * Creates a new name type for involved parties.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name type enum
	 * @param description    The description
	 * @param system         The system creating the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created name type
	 */
	default Uni<IInvolvedPartyNameType<?,?>> createNameType(Mutiny.Session session, Enum<?> name, String description, ISystems<?,?> system, UUID... identityToken){
		return createNameType(session, name.toString(), description, system, identityToken);
	}

	/**
	 * Creates a new name type by name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name type string
	 * @param description    The description
	 * @param system         The system creating the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created name type
	 */
	Uni<IInvolvedPartyNameType<?,?>> createNameType(Mutiny.Session session, String name, String description, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new identification type.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system creating the type
	 * @param name           The identification type enum
	 * @param description    The description
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created identification type
	 */
	default Uni<IInvolvedPartyIdentificationType<?,?>> createIdentificationType(Mutiny.Session session, ISystems<?,?> system, Enum<?> name, String description, UUID... identityToken)
	{
		return createIdentificationType(session, system, name.toString(), description, identityToken);
	}

	/**
	 * Creates a new identification type by name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system creating the type
	 * @param name           The identification type string
	 * @param description    The description
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created identification type
	 */
	Uni<IInvolvedPartyIdentificationType<?,?>> createIdentificationType(Mutiny.Session session, ISystems<?,?> system, String name, String description, UUID... identityToken);

	/**
	 * Creates a new involved party type.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system creating the type
	 * @param name           The type enum
	 * @param description    The description
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created involved party type
	 */
	default Uni<IInvolvedPartyType<?,?>> createType(Mutiny.Session session, ISystems<?,?> system, Enum<?> name, String description, UUID... identityToken)
	{
		return createType(session, system, name.toString(), description, identityToken);
	}

	/**
	 * Creates a new involved party type by name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system creating the type
	 * @param name           The type string
	 * @param description    The description
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created involved party type
	 */
	Uni<IInvolvedPartyType<?,?>> createType(Mutiny.Session session, ISystems<?,?> system, String name, String description, UUID... identityToken);


	/**
	 * Finds an identification type by enum.
	 *
	 * @param session        The Mutiny session to use
	 * @param idType         The identification type enum
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found identification type
	 */
	default Uni<IInvolvedPartyIdentificationType<?,?>> findInvolvedPartyIdentificationType(Mutiny.Session session, Enum<?> idType, ISystems<?,?> system, UUID... identityToken)
	{
		return findInvolvedPartyIdentificationType(session, idType.toString(), system, identityToken);
	}

	/**
	 * Finds an identification type by name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param idType         The identification type string
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found identification type
	 */
	Uni<IInvolvedPartyIdentificationType<?,?>> findInvolvedPartyIdentificationType(Mutiny.Session session, String idType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds an involved party by resource item and value.
	 *
	 * @param session        The Mutiny session to use
	 * @param idType         The resource item (e.g., an identification document)
	 * @param value          The value associated with the resource item
	 * @param system         The system searching for the party
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?,?>> findByResourceItem(Mutiny.Session session, IResourceItem<?,?> idType, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new involved party.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system creating the party
	 * @param idTypes        Pairs of identification type names and values
	 * @param isOrganic      Whether the party is an individual (true) or organization (false)
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created involved party
	 */
	Uni<IInvolvedParty<?,?>> create(Mutiny.Session session, ISystems<?,?> system, Pair<String, String> idTypes,
									boolean isOrganic, UUID... identityToken);

	/**
	 * Creates a new involved party with a specific key.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system creating the party
	 * @param key            The UUID key for the party
	 * @param idTypes        Pairs of identification type names and values
	 * @param isOrganic      Whether the party is an individual (true) or organization (false)
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created involved party
	 */
	Uni<IInvolvedParty<?, ?>> create(Mutiny.Session session, ISystems<?, ?> system, UUID key, Pair<String, String> idTypes,
									 boolean isOrganic, UUID... identityToken);

	/**
	 * Finds an involved party type by name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param type           The name of the type
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found involved party type
	 */
	Uni<IInvolvedPartyType<?,?>> findType(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds a name type by name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param nameType       The name of the name type
	 * @param system         The system searching for the type
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found name type
	 */
	Uni<IInvolvedPartyNameType<?,?>> findInvolvedPartyNameType(Mutiny.Session session, String nameType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds an involved party by security token.
	 *
	 * @param session        The Mutiny session to use
	 * @param token          The security token
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?,?>> findByToken(Mutiny.Session session, ISecurityToken<?,?> token, UUID... identityToken);

	/**
	 * Finds an involved party by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param uuid    The UUID of the party
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?,?>> find(Mutiny.Session session, UUID uuid);

	/**
	 * Finds an involved party type by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param uuid    The UUID of the type
	 * @return A Uni emitting the found type
	 */
	Uni<IInvolvedPartyType<?,?>> findType(Mutiny.Session session, UUID uuid);

	/**
	 * Finds a name type by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param uuid    The UUID of the name type
	 * @return A Uni emitting the found name type
	 */
	Uni<IInvolvedPartyNameType<?,?>> findNameType(Mutiny.Session session, UUID uuid);

	/**
	 * Finds an identification type by its unique ID.
	 *
	 * @param session The Mutiny session to use
	 * @param uuid    The UUID of the identification type
	 * @return A Uni emitting the found identification type
	 */
	Uni<IInvolvedPartyIdentificationType<?,?>> findIdentificationType(Mutiny.Session session, UUID uuid);

	/**
	 * Finds an involved party by UUID token.
	 *
	 * @param session        The Mutiny session to use
	 * @param token          The UUID token
	 * @param system         The system searching for the party
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?,?>> findByUUID(Mutiny.Session session, UUID token, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds all relationships by identification type and value.
	 *
	 * @param session             The Mutiny session to use
	 * @param identificationType  The identification type string
	 * @param value               The value to search for
	 * @return A Uni emitting a list of matching relationship values
	 */
	Uni<List<IRelationshipValue<IInvolvedParty<?,?>, IInvolvedPartyIdentificationType<?,?>, ?>>> findAllByIdentificationType(Mutiny.Session session, String identificationType, String value);

	/**
	 * Finds involved parties by rules classification.
	 *
	 * @param session         The Mutiny session to use
	 * @param classification  The classification name
	 * @param value           The classification value
	 * @param system          The system searching for parties
	 * @param identityToken   Optional security identity tokens
	 * @return A Uni emitting a list of found involved parties
	 */
	Uni<List<IInvolvedParty<?,?>>> findByRulesClassification(Mutiny.Session session, String classification, String value, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds an involved party by classification.
	 *
	 * @param session         The Mutiny session to use
	 * @param classification  The classification name
	 * @param value           The classification value
	 * @param system          The system searching for the party
	 * @param identityToken   Optional security identity tokens
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?,?>> findByClassification(Mutiny.Session session, String classification, String value, ISystems<?,?> system, UUID... identityToken);
}
