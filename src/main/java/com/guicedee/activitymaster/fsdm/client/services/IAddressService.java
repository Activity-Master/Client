package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address.IAddress;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.exceptions.AddressException;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


/**
 * Service interface for managing addresses and contact information.
 * This includes physical addresses, electronic addresses (email, web), and phone numbers.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IAddressService<J extends IAddressService<?>>
{
	/**
	 * The name of the Address system.
	 */
	String AddressSystemName = "Address System";

	/**
	 * Gets a new, uninitialized address instance.
	 *
	 * @return A new address instance
	 */
	IAddress<?,?> get();

	/**
	 * Creates a new address with the specified classification and value.
	 *
	 * @param session               The Mutiny session to use
	 * @param addressClassification The classification of the address
	 * @param system                The system creating the address
	 * @param value                 The address value
	 * @param identifyingToken      Optional security identity tokens
	 * @return A Uni emitting the created address
	 */
	Uni<IAddress<?,?>> create(Mutiny.Session session, String addressClassification, ISystems<?,?> system, String value, UUID... identifyingToken);

	/**
	 * Creates a new address with a specific key, classification, and value.
	 *
	 * @param session               The Mutiny session to use
	 * @param addressClassification The classification of the address
	 * @param key                   The UUID key for the address
	 * @param system                The system creating the address
	 * @param value                 The address value
	 * @param identifyingToken      Optional security identity tokens
	 * @return A Uni emitting the created address
	 */
	Uni<IAddress<?, ?>> create(Mutiny.Session session, String addressClassification, UUID key, ISystems<?, ?> system, String value, UUID... identifyingToken);

	/**
	 * Creates a new address that is <strong>scope-restricted</strong> rather than world-readable. Identical
	 * to {@link #create(Mutiny.Session, String, UUID, ISystems, String, UUID...)} except the address is
	 * secured with the restricted matrix: only Administrators / Systems / Applications / Plugins retain
	 * access, plus a <em>read</em> grant for {@code scopeToken}. Because the applicable-token climb is
	 * child&rarr;parent, only identity tokens located at the {@code scopeToken} node <em>or below it</em> may
	 * read the address.
	 *
	 * @param session               The Mutiny session to use
	 * @param addressClassification The classification of the address
	 * @param key                   The UUID key for the address, or {@code null} to generate one
	 * @param system                The system creating the address
	 * @param value                 The address value
	 * @param scopeToken            The scope token granted read on the new address
	 * @param identifyingToken      Optional security identity tokens
	 * @return A Uni emitting the created (scope-restricted) address
	 */
	Uni<IAddress<?, ?>> createScopeRestricted(Mutiny.Session session, String addressClassification, UUID key, ISystems<?, ?> system,
											  String value, ISecurityToken<?, ?> scopeToken, UUID... identifyingToken);

	/**
	 * Adds or finds an IP address.
	 *
	 * @param session          The Mutiny session to use
	 * @param ipAddress        The IP address string
	 * @param system           The system creating/finding the address
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the address
	 */
	Uni<IAddress<?,?>> addOrFindIPAddress(Mutiny.Session session, String ipAddress, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Adds or finds a host name.
	 *
	 * @param session          The Mutiny session to use
	 * @param hostName         The host name string
	 * @param system           The system creating/finding the address
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the address
	 */
	Uni<IAddress<?,?>> addOrFindHostName(Mutiny.Session session, String hostName, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Adds or finds a web address (URL).
	 *
	 * @param session          The Mutiny session to use
	 * @param webAddress       The web address string
	 * @param system           The system creating/finding the address
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the address
	 */
	Uni<IAddress<?,?>> addOrFindWebAddress(Mutiny.Session session, String webAddress, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Adds or finds a phone contact.
	 *
	 * @param session          The Mutiny session to use
	 * @param phoneNumber      The phone number string
	 * @param system           The system creating/finding the address
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the address
	 */
	Uni<IAddress<?,?>> addOrFindPhoneContact(Mutiny.Session session, String phoneNumber, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Adds or finds an email contact.
	 *
	 * @param session          The Mutiny session to use
	 * @param emailAddressString The email address string
	 * @param system           The system creating/finding the address
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the address
	 * @throws AddressException If the email address is invalid
	 */
	Uni<IAddress<?, ?>> addOrFindEmailContact(Mutiny.Session session, String emailAddressString, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/**
	 * Finds a cell phone contact for an involved party.
	 *
	 * @param session          The Mutiny session to use
	 * @param involvedParty    The involved party
	 * @param system           The system searching for the contact
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the relationship value containing the address
	 * @throws AddressException If an error occurs during the search
	 */
	Uni<IRelationshipValue<?, IAddress<?, ?>, ?>> findCellPhoneContact(Mutiny.Session session, IInvolvedParty<?, ?> involvedParty, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/**
	 * Adds or finds a street address.
	 *
	 * @param session          The Mutiny session to use
	 * @param number           The street number
	 * @param street           The street name
	 * @param streetType       The street type (e.g., St, Ave)
	 * @param system           The system creating/finding the address
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the address
	 */
	Uni<IAddress<?,?>> addOrFindStreetAddress(Mutiny.Session session, String number, String street, String streetType, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Adds or finds a postal address (e.g., PO Box).
	 *
	 * @param session          The Mutiny session to use
	 * @param boxIdentifier    The box identifier (e.g., "PO Box")
	 * @param boxNumber        The box number
	 * @param system           The system creating/finding the address
	 * @param identityToken    Optional security identity tokens
	 * @return A Uni emitting the address
	 */
	Uni<IAddress<?,?>> addOrFindPostalAddress(Mutiny.Session session, String boxIdentifier, String boxNumber, ISystems<?,?> system, UUID... identityToken);
}
