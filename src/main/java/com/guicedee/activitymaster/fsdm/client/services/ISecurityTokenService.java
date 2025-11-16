package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Date;
import java.util.UUID;


public interface ISecurityTokenService<J extends ISecurityTokenService<J>>
{
	String SecurityTokenSystemName = "Security Tokens System";

	ISecurityToken<?, ?> get();

	Uni<Void> grantAccessToToken(Mutiny.Session session, ISecurityToken<?,?> fromToken, ISecurityToken<?,?> toToken,
                                 boolean create, boolean update, boolean delete, boolean read, ISystems<?,?> system);

	Uni<Void> grantAccessToToken(Mutiny.Session session, @NotNull ISecurityToken<?,?> fromToken, @NotNull ISecurityToken<?,?> toToken,
								 boolean create, boolean update, boolean delete, boolean read,
								 ISystems<?,?> system, String originalId,
								 Date effectiveFromDate, Date effectiveToDate);

	Uni<ISecurityToken<?,?>> create(Mutiny.Session session, String classificationValue, String name, String description, ISystems<?,?> system);

	Uni<ISecurityToken<?,?>> create(Mutiny.Session session, String classificationValue, String name, String description, ISystems<?,?> system, ISecurityToken<?,?> parent, UUID... identityToken);

	Uni<Void> link(Mutiny.Session session, ISecurityToken<?,?> parent, ISecurityToken<?,?> child, IClassification<?,?> classification, String... identifyingToken);

	Uni<ISecurityToken<?,?>> getEveryoneGroup(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getEverywhereGroup(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getGuestsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getRegisteredGuestsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getVisitorsGuestsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getAdministratorsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getSystemsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getPluginsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getApplicationsFolder(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getSecurityToken(Mutiny.Session session, UUID identifyingToken, ISystems<?,?> system, UUID... identityToken);

	Uni<ISecurityToken<?,?>> getSecurityToken(Mutiny.Session session, UUID identifyingToken, boolean overrideActiveFlag, ISystems<?,?> system, UUID... identityToken);
}
