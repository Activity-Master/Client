package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassificationDataConcept;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import com.entityassist.enumerations.ActiveFlag;
import com.guicedee.client.IGuiceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public interface IClassificationDataConceptService<J extends IClassificationDataConceptService<J>> {
    String ClassificationDataConceptSystemName = "Classification Data Concept System";

    IClassificationDataConcept<?, ?> get();

    Uni<IClassificationDataConcept<?, ?>> createDataConcept(Mutiny.Session session, EnterpriseClassificationDataConcepts name,
                                                            String description,
                                                            ISystems<?, ?> system,
                                                            UUID... identityToken);

    Uni<IClassificationDataConcept<?, ?>> getGlobalConcept(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    Uni<IClassificationDataConcept<?, ?>> find(Mutiny.Session session, EnterpriseClassificationDataConcepts name, ISystems<?, ?> system, UUID... identityToken);

    Uni<IClassificationDataConcept<?, ?>> getNoConcept(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    Uni<IClassificationDataConcept<?, ?>> getSecurityHierarchyConcept(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Resolves a Classification Data Concept ID (UUID) by its unique name within an enterprise and system.
     * Contract: never returns null. If not found, lets NoResultException propagate.
     */
    Uni<UUID> resolveCdcIdByName(Mutiny.Session session, IEnterprise<?, ?> enterpriseId, UUID systemId, String conceptName);
}
