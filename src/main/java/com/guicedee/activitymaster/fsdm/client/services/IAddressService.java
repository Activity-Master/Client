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

	// ---- Stateless (Mutiny.StatelessSession) twins (Address is non-cacheable + LAZY → stateless-safe). ----

	/** Stateless variant of {@link #create(Mutiny.StatelessSession, String, ISystems, String, UUID...)}. */
	Uni<IAddress<?, ?>> create(Mutiny.StatelessSession session, String addressClassification, ISystems<?, ?> system, String value, UUID... identifyingToken);

	/** Stateless variant of {@link #create(Mutiny.StatelessSession, String, UUID, ISystems, String, UUID...)}. */
	Uni<IAddress<?, ?>> create(Mutiny.StatelessSession session, String addressClassification, UUID key, ISystems<?, ?> system, String value, UUID... identifyingToken);

	/** Stateless variant of {@link #createScopeRestricted(Mutiny.StatelessSession, String, UUID, ISystems, String, ISecurityToken, UUID...)}. */
	Uni<IAddress<?, ?>> createScopeRestricted(Mutiny.StatelessSession session, String addressClassification, UUID key, ISystems<?, ?> system,
											  String value, ISecurityToken<?, ?> scopeToken, UUID... identifyingToken);

	/** Stateless variant of {@link #addOrFindIPAddress(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IAddress<?, ?>> addOrFindIPAddress(Mutiny.StatelessSession session, String ipAddress, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/** Stateless variant of {@link #addOrFindHostName(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IAddress<?, ?>> addOrFindHostName(Mutiny.StatelessSession session, String hostName, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/** Stateless variant of {@link #addOrFindWebAddress(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IAddress<?, ?>> addOrFindWebAddress(Mutiny.StatelessSession session, String webAddress, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/** Stateless variant of {@link #addOrFindPhoneContact(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IAddress<?, ?>> addOrFindPhoneContact(Mutiny.StatelessSession session, String phoneNumber, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/** Stateless variant of {@link #addOrFindEmailContact(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
	Uni<IAddress<?, ?>> addOrFindEmailContact(Mutiny.StatelessSession session, String emailAddressString, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/** Stateless variant of {@link #addOrFindStreetAddress(Mutiny.StatelessSession, String, String, String, ISystems, UUID...)}. */
	Uni<IAddress<?, ?>> addOrFindStreetAddress(Mutiny.StatelessSession session, String number, String street, String streetType, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/** Stateless variant of {@link #addOrFindPostalAddress(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
	Uni<IAddress<?, ?>> addOrFindPostalAddress(Mutiny.StatelessSession session, String boxIdentifier, String boxNumber, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	/** Stateless variant of {@link #findCellPhoneContact(Mutiny.StatelessSession, IInvolvedParty, ISystems, UUID...)}. */
	Uni<IRelationshipValue<?, IAddress<?, ?>, ?>> findCellPhoneContact(Mutiny.StatelessSession session, IInvolvedParty<?, ?> involvedParty, ISystems<?, ?> system, UUID... identityToken) throws AddressException;
}
