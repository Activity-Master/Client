package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address.IAddress;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.exceptions.AddressException;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


public interface IAddressService<J extends IAddressService<?>>
{
	String AddressSystemName = "Address System";

	IAddress<?,?> get();

	Uni<IAddress<?,?>> create(Mutiny.Session session, String addressClassification, ISystems<?,?> system, String value, UUID... identifyingToken);

	Uni<IAddress<?, ?>> create(Mutiny.Session session, String addressClassification, UUID key, ISystems<?, ?> system, String value, UUID... identifyingToken);

	Uni<IAddress<?,?>> addOrFindIPAddress(Mutiny.Session session, String ipAddress, ISystems<?,?> system, UUID... identityToken);

	Uni<IAddress<?,?>> addOrFindHostName(Mutiny.Session session, String hostName, ISystems<?,?> system, UUID... identityToken);

	Uni<IAddress<?,?>> addOrFindWebAddress(Mutiny.Session session, String webAddress, ISystems<?,?> system, UUID... identityToken);

	Uni<IAddress<?,?>> addOrFindPhoneContact(Mutiny.Session session, String phoneNumber, ISystems<?,?> system, UUID... identityToken);

	Uni<IAddress<?, ?>> addOrFindEmailContact(Mutiny.Session session, String emailAddressString, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	Uni<IRelationshipValue<?, IAddress<?, ?>, ?>> findCellPhoneContact(Mutiny.Session session, IInvolvedParty<?, ?> involvedParty, ISystems<?, ?> system, UUID... identityToken) throws AddressException;

	Uni<IAddress<?,?>> addOrFindStreetAddress(Mutiny.Session session, String number, String street, String streetType, ISystems<?,?> system, UUID... identityToken);

	Uni<IAddress<?,?>> addOrFindPostalAddress(Mutiny.Session session, String boxIdentifier, String boxNumber, ISystems<?,?> system, UUID... identityToken);
}
