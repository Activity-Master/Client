package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassificationDataConcept;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


public interface IClassificationDataConceptService<J extends IClassificationDataConceptService<J>>
{
	String ClassificationDataConceptSystemName = "Classification Data Concept System";

/*
	Uni<IClassificationDataConcept<?,?>> createDataConcept(EnterpriseClassificationDataConcepts name,
	                                            String description,
	                                            ISystems<?,?> system, java.util.UUID... identityToken);
*/

	IClassificationDataConcept<?,?> get();

  Uni<IClassificationDataConcept<?, ?>> createDataConcept(Mutiny.Session session, EnterpriseClassificationDataConcepts name,
                                                          String description,
                                                          ISystems<?, ?> system,
                                                          UUID... identityToken);

  Uni<IClassificationDataConcept<?,?>> getGlobalConcept(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassificationDataConcept<?,?>> find(Mutiny.Session session, EnterpriseClassificationDataConcepts name, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassificationDataConcept<?,?>> getNoConcept(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<IClassificationDataConcept<?,?>> getSecurityHierarchyConcept(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);
}
