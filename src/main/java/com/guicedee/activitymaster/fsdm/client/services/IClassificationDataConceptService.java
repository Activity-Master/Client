package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassificationDataConcept;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import com.entityassist.enumerations.ActiveFlag;
import com.guicedee.client.IGuiceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public interface IClassificationDataConceptService<J extends IClassificationDataConceptService<J>>
{
	String ClassificationDataConceptSystemName = "Classification Data Concept System";

	IClassificationDataConcept<?,?> get();

  Uni<IClassificationDataConcept<?, ?>> createDataConcept(Mutiny.Session session, EnterpriseClassificationDataConcepts name,
                                                          String description,
                                                          ISystems<?, ?> system,
                                                          UUID... identityToken);

  Uni<IClassificationDataConcept<?,?>> getGlobalConcept(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassificationDataConcept<?,?>> find(Mutiny.Session session, EnterpriseClassificationDataConcepts name, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassificationDataConcept<?,?>> getNoConcept(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassificationDataConcept<?,?>> getSecurityHierarchyConcept(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

    /**
     * Resolves a Classification Data Concept ID (UUID) by its unique name within an enterprise and system.
     * Contract: never returns null. If not found, lets NoResultException propagate.
     */
    default Uni<UUID> resolveCdcIdByName(Mutiny.Session session, UUID enterpriseId, UUID systemId, String conceptName) {
        return com.guicedee.activitymaster.fsdm.client.services.cache.NameIdCache
                .getClassificationDataConceptId(session, enterpriseId, systemId, conceptName, (sess, name) -> {
                    // Get the visible range IDs from the ActiveFlag service (via Guice context) and query with IN clause
                    var afService = IGuiceContext.get(IActiveFlagService.class);
                    return afService.getVisibleRangeAndUpIds(sess, enterpriseId)
                            .flatMap(visibleIds -> {
                                String sql = "select classificationdataconceptid from classification.classificationdataconcept " +
                                        "where enterpriseid = :ent and classificationdataconceptname = :name " +
                                        "and (effectivefromdate <= current_timestamp) " +
                                        "and (effectivetodate > current_timestamp) " +
                                        "and activeflagid in (:visibleIds)";
                                return sess.createNativeQuery(sql)
                                        .setParameter("ent", enterpriseId)
                                        .setParameter("name", name)
                                        .setParameter("visibleIds", visibleIds)
                                        .getSingleResult()
                                        .map(result -> (UUID) result);
                            });
                });
    }
}
