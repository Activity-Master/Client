package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangementType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IArrangementsService<J extends IArrangementsService<J>>
{
	String ArrangementSystemName = "Arrangements System";

	default
	Uni<IArrangement<?,?>> create(Mutiny.Session session, Enum<?> type,
								  Enum<?> arrangementTypeClassification,
								  String arrangementTypeValue,
								  ISystems<?,?> system,
								  UUID... identityToken)
	{
		return create(session, type.toString(), arrangementTypeClassification.toString(), arrangementTypeValue, system, identityToken);
	}

	IArrangement<?,?> get();

	Uni<IArrangement<?,?>> create(Mutiny.Session session, String type,
                                  String arrangementTypeClassification,
                                  String arrangementTypeValue,
                                  ISystems<?,?> system,
                                  UUID... identityToken);

	default Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.Session session, Enum<?> type, ISystems<?,?> system, UUID... identityToken)
	{
		return createArrangementType(session, type.toString(), system, identityToken);
	}

	Uni<IArrangement<?,?>> create(Mutiny.Session session, String type, UUID key,
								  String arrangementTypeClassification,
								  String arrangementTypeValue,
								  ISystems<?, ?> system,
								  UUID... identityToken);

	Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);

	Uni<IArrangementType<?, ?>> createArrangementType(Mutiny.Session session, String type, UUID key, ISystems<?, ?> system, UUID... identityToken);

	Uni<IArrangementType<?, ?>> findArrangementType(Mutiny.Session session, String type, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findInvolvedPartyArrangements(Mutiny.Session session, IInvolvedParty<?,?> ip, String arrType, ISystems<?,?> systems, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.Session session, String arrType, String value, ISystems<?,?> systems, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGT(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGTE(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationGTEWithIP(Mutiny.Session session, String arrangementType, String classificationName,
																		   IInvolvedParty<?,?> withInvolvedParty, String ipClassification, IArrangement<?,?> withParent,
																		   IResourceItem<?,?> resourceItem,
																		   String resourceItemClassification,
																		   String value, ISystems<?,?> system, UUID... identityToken);


	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLT(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByClassificationLTE(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByClassification(Mutiny.Session session, String arrType, IArrangement<?,?> withParent, String value, ISystems<?,?> systems, UUID... identityToken);

	Uni<IArrangement<?,?>> findArrangementByResourceItem(Mutiny.Session session, IResourceItem<?,?> resourceItem, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<IArrangement<?,?>> findArrangementByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByRulesType(Mutiny.Session session, IRulesType<?,?> ruleType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, LocalDateTime startDate, LocalDateTime endDate, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IArrangement<?,?>>> findArrangementsByInvolvedParty(Mutiny.Session session, IInvolvedParty<?,?> involvedParty, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IInvolvedParty<?,?>>> findArrangementInvolvedParties(Mutiny.Session session, IArrangement<?,?> arrangement, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<@NotNull IArrangementType<?,?>> find(Mutiny.Session session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	Uni<@NotNull IArrangement<?,?>> find(Mutiny.Session session, UUID id, ISystems<?,?> system, UUID... identityToken);

	Uni<@NotNull IArrangement<?,?>> find(Mutiny.Session session, UUID id);

	Uni<@NotNull List<IArrangement<?,?>>> findAll(Mutiny.Session session, String arrangementType, ISystems<?,?> system, UUID... identityToken);

	Uni<@NotNull IArrangement<?,?>> completeArrangement(Mutiny.Session session, IArrangement<?,?> arrangement, ISystems<?,?> system, UUID... identityToken);

}
