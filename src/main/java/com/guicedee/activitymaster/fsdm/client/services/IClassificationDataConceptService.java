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

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins. ClassificationDataConcept is @Cacheable with no eager
    // @ManyToOne, so the prepped scalar-projection reads + session.insert are stateless-safe.
    // =============================================================================================

    /** Stateless prepped variant of {@link #find(Mutiny.StatelessSession, EnterpriseClassificationDataConcepts, ISystems, UUID...)} (by name). */
    Uni<IClassificationDataConcept<?, ?>> find(Mutiny.StatelessSession session, String name, ISystems<?, ?> system, UUID... identityToken);

    /** Enum-name stateless variant of {@link #find(Mutiny.StatelessSession, String, ISystems, UUID...)}. */
    default Uni<IClassificationDataConcept<?, ?>> find(Mutiny.StatelessSession session, EnterpriseClassificationDataConcepts name, ISystems<?, ?> system, UUID... identityToken) {
        return find(session, name.classificationValue(), system, identityToken);
    }

    /** Stateless find-or-create variant of {@link #createDataConcept(Mutiny.StatelessSession, EnterpriseClassificationDataConcepts, String, ISystems, UUID...)}. */
    Uni<IClassificationDataConcept<?, ?>> createDataConcept(Mutiny.StatelessSession session, EnterpriseClassificationDataConcepts name,
                                                            String description, ISystems<?, ?> system, UUID... identityToken);

    /** Stateless variant of {@link #getGlobalConcept(Mutiny.StatelessSession, ISystems, UUID...)}. */
    default Uni<IClassificationDataConcept<?, ?>> getGlobalConcept(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken) {
        return find(session, EnterpriseClassificationDataConcepts.GlobalClassificationsDataConceptName, system, identityToken);
    }

    /** Stateless variant of {@link #getNoConcept(Mutiny.StatelessSession, ISystems, UUID...)}. */
    default Uni<IClassificationDataConcept<?, ?>> getNoConcept(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken) {
        return find(session, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, identityToken);
    }

    /** Stateless variant of {@link #getSecurityHierarchyConcept(Mutiny.StatelessSession, ISystems, UUID...)}. */
    default Uni<IClassificationDataConcept<?, ?>> getSecurityHierarchyConcept(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken) {
        return find(session, EnterpriseClassificationDataConcepts.SecurityTokenXSecurityToken, system, identityToken);
    }
}
