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


/**
 * Service interface for managing classification data concepts.
 * Data concepts provide high-level grouping and meaning for classifications.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IClassificationDataConceptService<J extends IClassificationDataConceptService<J>> {
    /**
     * The name of the Classification Data Concept system.
     */
    String ClassificationDataConceptSystemName = "Classification Data Concept System";

    /**
     * Gets a new, uninitialized classification data concept instance.
     *
     * @return A new classification data concept instance
     */
    IClassificationDataConcept<?, ?> get();

    /**
     * Creates a new classification data concept.
     *
     * @param session       The Mutiny session to use
     * @param name          The data concept enum
     * @param description   The description
     * @param system        The system creating the concept
     * @param identityToken Optional security identity tokens
     * @return A Uni emitting the created data concept
     */
    Uni<IClassificationDataConcept<?, ?>> createDataConcept(Mutiny.Session session, EnterpriseClassificationDataConcepts name,
                                                            String description,
                                                            ISystems<?, ?> system,
                                                            UUID... identityToken);

    /**
     * Gets the global data concept.
     *
     * @param session       The Mutiny session to use
     * @param system        The system searching for the concept
     * @param identityToken Optional security identity tokens
     * @return A Uni emitting the global data concept
     */
    Uni<IClassificationDataConcept<?, ?>> getGlobalConcept(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Finds a classification data concept by its name enum.
     *
     * @param session       The Mutiny session to use
     * @param name          The data concept enum
     * @param system        The system searching for the concept
     * @param identityToken Optional security identity tokens
     * @return A Uni emitting the found data concept
     */
    Uni<IClassificationDataConcept<?, ?>> find(Mutiny.Session session, EnterpriseClassificationDataConcepts name, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Gets the default 'No Concept' instance.
     *
     * @param session       The Mutiny session to use
     * @param system        The system searching for the concept
     * @param identityToken Optional security identity tokens
     * @return A Uni emitting the 'No Concept' instance
     */
    Uni<IClassificationDataConcept<?, ?>> getNoConcept(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

    /**
     * Gets the security hierarchy data concept.
     *
     * @param session       The Mutiny session to use
     * @param system        The system searching for the concept
     * @param identityToken Optional security identity tokens
     * @return A Uni emitting the security hierarchy data concept
     */
    Uni<IClassificationDataConcept<?, ?>> getSecurityHierarchyConcept(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);
}
