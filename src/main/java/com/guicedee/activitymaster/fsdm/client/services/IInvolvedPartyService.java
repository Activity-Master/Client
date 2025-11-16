package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.guicedinjection.pairing.Pair;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.UUID;


public interface IInvolvedPartyService<J extends IInvolvedPartyService<J>>
{
	String InvolvedPartySystemName = "Involved Party System";

	IInvolvedParty<?,?> get();

	Uni<IInvolvedParty<?,?>> findByID(Mutiny.Session session, UUID id);

	default Uni<IInvolvedPartyNameType<?,?>> createNameType(Mutiny.Session session, Enum<?> name, String description, ISystems<?,?> system, UUID... identityToken){
		return createNameType(session, name.toString(), description, system, identityToken);
	}
	Uni<IInvolvedPartyNameType<?,?>> createNameType(Mutiny.Session session, String name, String description, ISystems<?,?> system, UUID... identityToken);

	default Uni<IInvolvedPartyIdentificationType<?,?>> createIdentificationType(Mutiny.Session session, ISystems<?,?> system, Enum<?> name, String description, UUID... identityToken)
	{
		return createIdentificationType(session, system, name.toString(), description, identityToken);
	}

	Uni<IInvolvedPartyIdentificationType<?,?>> createIdentificationType(Mutiny.Session session, ISystems<?,?> system, String name, String description, UUID... identityToken);

	default Uni<IInvolvedPartyType<?,?>> createType(Mutiny.Session session, ISystems<?,?> system, Enum<?> name, String description, UUID... identityToken)
	{
		return createType(session, system, name.toString(), description, identityToken);
	}
	Uni<IInvolvedPartyType<?,?>> createType(Mutiny.Session session, ISystems<?,?> system, String name, String description, UUID... identityToken);


	default Uni<IInvolvedPartyIdentificationType<?,?>> findInvolvedPartyIdentificationType(Mutiny.Session session, Enum<?> idType, ISystems<?,?> system, UUID... identityToken)
	{
		return findInvolvedPartyIdentificationType(session, idType.toString(), system, identityToken);
	}

	Uni<IInvolvedPartyIdentificationType<?,?>> findInvolvedPartyIdentificationType(Mutiny.Session session, String idType, ISystems<?,?> system, UUID... identityToken);

	Uni<IInvolvedParty<?,?>> findByResourceItem(Mutiny.Session session, IResourceItem<?,?> idType, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<IInvolvedParty<?,?>> create(Mutiny.Session session, ISystems<?,?> system, Pair<String, String> idTypes,
									boolean isOrganic, UUID... identityToken);

	Uni<IInvolvedParty<?, ?>> create(Mutiny.Session session, ISystems<?, ?> system, UUID key, Pair<String, String> idTypes,
									 boolean isOrganic, UUID... identityToken);

	Uni<IInvolvedPartyType<?,?>> findType(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);

	Uni<IInvolvedPartyNameType<?,?>> findInvolvedPartyNameType(Mutiny.Session session, String nameType, ISystems<?,?> system, UUID... identityToken);

	Uni<IInvolvedParty<?,?>> findByToken(Mutiny.Session session, ISecurityToken<?,?> token, UUID... identityToken);

	Uni<IInvolvedParty<?,?>> find(Mutiny.Session session, UUID uuid);

	Uni<IInvolvedPartyType<?,?>> findType(Mutiny.Session session, UUID uuid);

	Uni<IInvolvedPartyNameType<?,?>> findNameType(Mutiny.Session session, UUID uuid);

	Uni<IInvolvedPartyIdentificationType<?,?>> findIdentificationType(Mutiny.Session session, UUID uuid);

	Uni<IInvolvedParty<?,?>> findByUUID(Mutiny.Session session, UUID token, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRelationshipValue<IInvolvedParty<?,?>, IInvolvedPartyIdentificationType<?,?>, ?>>> findAllByIdentificationType(Mutiny.Session session, String identificationType, String value);

	Uni<List<IInvolvedParty<?,?>>> findByRulesClassification(Mutiny.Session session, String classification, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<IInvolvedParty<?,?>> findByClassification(Mutiny.Session session, String classification, String value, ISystems<?,?> system, UUID... identityToken);
}
