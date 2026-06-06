package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


/**
 * Service interface for managing classifications.
 * Classifications are used to categorize and organize data within the system.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IClassificationService<J extends IClassificationService<J>>
{
	/**
	 * The name of the Classifications system.
	 */
	String ClassificationSystemName = "Classifications System";

	/**
	 * Creates a new classification.
	 *
	 * @param session         The Mutiny session to use
	 * @param name            The name of the classification
	 * @param description     The description of the classification
	 * @param concept         The data concept associated with the classification
	 * @param system          The system creating the classification
	 * @param sequenceOrder   The sequence order for display
	 * @param parentName      The name of the parent classification
	 * @param identityToken   Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts concept,
									 ISystems<?,?> system, Integer sequenceOrder, String parentName, UUID... identityToken);

	/**
	 * Creates a new classification using an enum for the name.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param system         The system creating the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name,
											 ISystems<?,?> system, UUID... identityToken)
	{
		return create(session, name.toString(), system, identityToken);
	}

	/**
	 * Creates a new classification using an enum for the name and a description.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param description    The description of the classification
	 * @param system         The system creating the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name, String description,
											 ISystems<?,?> system, UUID... identityToken)
	{
		return create(session, name.toString(), description, system, identityToken);
	}

	/**
	 * Creates a new classification using an enum for the name and a data concept.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param concept        The data concept
	 * @param system         The system creating the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	default Uni<IClassification<?,?>> create(Mutiny.Session session, Enum<?> name, EnterpriseClassificationDataConcepts concept,
											 ISystems<?,?> system, UUID... identityToken)
	{
		return create(session, name.toString(), name.toString(), concept, system, identityToken);
	}

	/**
	 * Creates a new classification with a parent enum.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param system         The system creating the classification
	 * @param parent         The parent classification enum
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
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

	/**
	 * Creates a new classification with a description and a parent enum.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param description    The description
	 * @param system         The system creating the classification
	 * @param parent         The parent classification enum
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
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

	/**
	 * Creates a new classification with a parent enum and a data concept.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param system         The system creating the classification
	 * @param parent         The parent classification enum
	 * @param concept        The data concept
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
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

	/**
	 * Creates a new classification with a parent name.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param system         The system creating the classification
	 * @param parent         The name of the parent classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
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

	/**
	 * Creates a new classification by name.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param system         The system creating the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	Uni<IClassification<?,?>> create(Mutiny.Session session, String name,
									 ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new classification by name and description.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param description    The description
	 * @param system         The system creating the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description,
									 ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new classification by name, description, and concept.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param description    The description
	 * @param conceptName    The data concept
	 * @param system         The system creating the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts conceptName,
									 ISystems<?,?> system, UUID... identityToken);

	/**
	 * Creates a new classification with sequence number.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param description    The description
	 * @param conceptName    The data concept
	 * @param system         The system creating the classification
	 * @param sequenceNumber The sequence number for ordering
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts conceptName,
									 ISystems<?,?> system,
									 Integer sequenceNumber, UUID... identityToken);

	/**
	 * Creates a new classification with sequence number and parent classification object.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param description    The description
	 * @param conceptName    The data concept
	 * @param system         The system creating the classification
	 * @param sequenceNumber The sequence number for ordering
	 * @param parent         The parent classification instance
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created classification
	 */
	Uni<IClassification<?,?>> create(Mutiny.Session session, String name, String description, EnterpriseClassificationDataConcepts conceptName,
									 ISystems<?,?> system,
									 Integer sequenceNumber, IClassification<?,?> parent, UUID... identityToken);

	/**
	 * Creates a new classification that is <strong>scope-restricted</strong> rather than world-readable.
	 * Identical to {@link #create(Mutiny.Session, String, String, EnterpriseClassificationDataConcepts, ISystems, Integer, IClassification, UUID...)}
	 * except the classification is secured with the restricted matrix: only Administrators / Systems /
	 * Applications / Plugins retain access, plus a <em>read</em> grant for {@code scopeToken}. Because the
	 * applicable-token climb is child&rarr;parent, only identity tokens located at the {@code scopeToken}
	 * node <em>or below it</em> may read the classification.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param description    The description
	 * @param conceptName    The data concept
	 * @param system         The system creating the classification
	 * @param sequenceNumber The sequence number for ordering
	 * @param parent         The parent classification instance, or {@code null}
	 * @param scopeToken     The scope token granted read on the new classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the created (scope-restricted) classification
	 */
	Uni<IClassification<?,?>> createScopeRestricted(Mutiny.Session session, String name, String description,
													EnterpriseClassificationDataConcepts conceptName, ISystems<?,?> system,
													Integer sequenceNumber, IClassification<?,?> parent,
													com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken<?,?> scopeToken,
													UUID... identityToken);

	/**
	 * Finds a classification by enum name.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param system         The system searching for the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found classification
	 */
	default Uni<IClassification<?,?>> find(Mutiny.Session session, Enum<?> name, ISystems<?,?> system, UUID... identityToken) {
		return find(session, name.toString(), system, identityToken);
	}

	/**
	 * Finds a classification by name string.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param system         The system searching for the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found classification
	 */
	Uni<IClassification<?,?>> find(Mutiny.Session session, String name, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Finds a classification by enum name and data concept.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The enum value for the name
	 * @param concept        The data concept
	 * @param system         The system searching for the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found classification
	 */
	default Uni<IClassification<?,?>> find(Mutiny.Session session, Enum<?> name, EnterpriseClassificationDataConcepts concept, ISystems<?,?> system,
										   UUID... identityToken) {
		return find(session, name.toString(), concept, system, identityToken);
	}

	/**
	 * Finds a classification by name string and data concept.
	 *
	 * @param session        The Mutiny session to use
	 * @param name           The name of the classification
	 * @param concept        The data concept
	 * @param system         The system searching for the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found classification
	 */
	Uni<IClassification<?,?>> find(Mutiny.Session session, String name, EnterpriseClassificationDataConcepts concept, ISystems<?,?> system,
								   UUID... identityToken);

	/**
	 * Gets the classification for hierarchy types.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the hierarchy type classification
	 */
	Uni<IClassification<?,?>> getHierarchyType(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the default 'No Classification' instance.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the 'No Classification' instance
	 */
	Uni<IClassification<?,?>> getNoClassification(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Gets the classification for identity types.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system searching for the classification
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the identity type classification
	 */
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
