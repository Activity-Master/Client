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

    /**
     * Resolves a Classification ID (UUID) by its unique name using a lightweight native SQL lookup
     * with a small in-memory cache to reduce database load. When systemId/conceptId are provided, the
     * lookup is further constrained; otherwise, it falls back to an enterprise+name lookup.
     */
    default Uni<UUID> resolveClassificationIdByName(Mutiny.Session session,
                                                    UUID enterpriseId,
                                                    UUID systemId,
                                                    UUID conceptId,
                                                    String classificationName) {
        // When either systemId or conceptId is missing, resolve by enterprise+name only
        if (systemId == null || conceptId == null) {
            return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                    .getClassificationId(session, enterpriseId, null, null, classificationName, (sess, name) -> {
                        String sql = "select classificationid from classification.classification " +
                                     "where enterpriseid = :ent and classificationname = :name " +
                                     "and (effectivefromdate <= current_timestamp) " +
                                     "and (effectivetodate > current_timestamp) " +
                                     "and activeflagid = (select activeflagid from dbo.activeflag where enterpriseid = :ent and activeflagname = 'Active')";
                        return sess.createNativeQuery(sql)
                                   .setParameter("ent", enterpriseId)
                                   .setParameter("name", name)
                                   .getSingleResult()
                                   .map(result -> (UUID) result);
                    });
        }

        // Full scope: enterprise + system + concept + name
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getClassificationId(session, enterpriseId, systemId, conceptId, classificationName, (sess, name) -> {
                    String sql = "select classificationid from classification.classification " +
                                 "where enterpriseid = :ent and classificationdataconceptid = :cdc and classificationname = :name " +
                                 "and (effectivefromdate <= current_timestamp) " +
                                 "and (effectivetodate > current_timestamp) " +
                                 "and activeflagid = (select activeflagid from dbo.activeflag where enterpriseid = :ent and activeflagname = 'Active')";
                    return sess.createNativeQuery(sql)
                               .setParameter("ent", enterpriseId)
                              // .setParameter("sys", systemId)
                               .setParameter("cdc", conceptId)
                               .setParameter("name", name)
                               .getSingleResult()
                               .map(result -> (UUID) result);
                });
    }
}
