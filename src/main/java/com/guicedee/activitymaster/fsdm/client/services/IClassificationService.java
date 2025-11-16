package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


public interface IClassificationService<J extends IClassificationService<J>>
{
	String ClassificationSystemName = "Classifications System";

	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts concept,
									 ISystems<?,?> system, Integer sequenceOrder, String parentName, UUID... identityToken);

	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name,
											 ISystems<?,?> system, UUID... identityToken)
	{
		return create(session, name.toString(), system, identityToken);
	}

	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name, String description,
											 ISystems<?,?> system, UUID... identityToken)
	{
		return create(session, name.toString(), description, system, identityToken);
	}

	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name, EnterpriseClassificationDataConcepts concept,
											 ISystems<?,?> system, UUID... identityToken)
	{
		return create(session, name.toString(), name.toString(), concept, system, identityToken);
	}

	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name,
											 ISystems<?,?> system, Enum<?> parent, UUID... identityToken)
		{
			if (parent == null)
			{
				return create(session, name.toString(),
							name.toString(),
							EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, 0, identityToken);
			}
			return create(session, parent.toString(), system, identityToken)
		        .chain(classification -> create(session, name.toString(),
						name.toString(),
						EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, 0, classification, identityToken));
		}

	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name, String description,
											 ISystems<?,?> system, Enum<?> parent, UUID... identityToken)
	{
		if (parent == null)
		{
			return create(session, name.toString(),
					description,
					EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, 0, identityToken);
		}
		return create(session, parent.toString(), system, identityToken)
		        .chain(classification -> create(session, name.toString(),
						description,
						EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, 0, classification, identityToken));
	}

	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name,
											 ISystems<?,?> system, Enum<?> parent, EnterpriseClassificationDataConcepts concept,
											 UUID... identityToken)
	{
		if (parent == null)
		{
			return create(session, name.toString(), name.toString(),
					concept, system, 0, identityToken);
		}
		return create(session, parent.toString(), system, identityToken)
		        .chain(classification -> create(session, name.toString(), name.toString(),
						concept, system, 0, classification, identityToken));
	}

	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name,
											 ISystems<?,?> system, String parent, UUID... identityToken)
	{
		if (parent == null || parent.isBlank())
		{
			return create(session, name.toString(),
					name.toString(),
					EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, 0, identityToken);
		}
		return create(session, parent, system, identityToken)
		        .chain(classification -> create(session, name.toString(),
						name.toString(),
						EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, 0, classification, identityToken));
	}

	Uni<IClassification<?,?>> create(Mutiny.Session session, String name,
									 ISystems<?,?> system, UUID... identityToken);

	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description,
									 ISystems<?,?> system, UUID... identityToken);

	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts conceptName,
									 ISystems<?,?> system, UUID... identityToken);

	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts conceptName,
									 ISystems<?,?> system,
									 Integer sequenceNumber, UUID... identityToken);

	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts conceptName,
									 ISystems<?,?> system,
									 Integer sequenceNumber, IClassification<?,?> parent, UUID... identityToken);

	default Uni<IClassification<?,?>> find(Mutiny.Session session, Enum<?> name, ISystems<?,?> system, UUID... identityToken) {
		return find(session, name.toString(), system, identityToken);
	}

	Uni<IClassification<?,?>> find(Mutiny.Session session, String name, ISystems<?,?> system, UUID... identityToken);

	default Uni<IClassification<?,?>> find(Mutiny.Session session, Enum<?> name, EnterpriseClassificationDataConcepts concept, ISystems<?,?> system,
										   UUID... identityToken) {
		return find(session, name.toString(), concept, system, identityToken);
	}

	Uni<IClassification<?,?>> find(Mutiny.Session session, String name, EnterpriseClassificationDataConcepts concept, ISystems<?,?> system,
								   UUID... identityToken);

	Uni<IClassification<?,?>> getHierarchyType(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassification<?,?>> getNoClassification(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassification<?,?>> getIdentityType(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);
}
