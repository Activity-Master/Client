package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IProgressable;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.UUID;


/**
 * Service interface for managing user passwords and authentication.
 *
 * @param <J> The type of the service that implements this interface
 */
public interface IPasswordsService<J extends IPasswordsService<J>> extends IProgressable
{
	/**
	 * Finds an involved party by their username.
	 *
	 * @param session        The Mutiny session to use
	 * @param username       The username to search for
	 * @param system         The system performing the search
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?, ?>> findByUsername(Mutiny.StatelessSession session, String username, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Finds an involved party by username and password (authentication).
	 *
	 * @param session        The Mutiny session to use
	 * @param username       The username
	 * @param password       The password
	 * @param system         The system performing the search
	 * @param throwForNoUser Whether to throw an exception if the user is not found
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the found involved party
	 */
	Uni<IInvolvedParty<?, ?>> findByUsernameAndPassword(Mutiny.StatelessSession session, String username, String password, ISystems<?, ?> system, boolean throwForNoUser, UUID... identityToken);

	/** Stateless variant of {@link #getAllUsers(Mutiny.StatelessSession, ISystems, UUID...)} — projects ids and preps detached parties. */
	Uni<List<IInvolvedParty<?, ?>>> getAllUsers(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID... identityToken);

	/** Stateless variant of {@link #addUpdateUsernamePassword(Mutiny.StatelessSession, String, String, IInvolvedParty, ISystems, UUID...)}. */
	Uni<IInvolvedParty<?, ?>> addUpdateUsernamePassword(Mutiny.StatelessSession session, String username, String password, IInvolvedParty<?, ?> involvedParty, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Checks if a username already exists.
	 *
	 * @param session        The Mutiny session to use
	 * @param username       The username to check
	 * @param system         The system performing the check
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting true if the username exists, false otherwise
	 */
	Uni<Boolean> doesUsernameExist(Mutiny.StatelessSession session, String username, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Stateless variant of {@link #createAdminAndCreatorUserForEnterprise(Mutiny.StatelessSession, ISystems, String, String, UUID)}
	 * — provisions the enterprise creator/administrator involved party, its identification/name/party types,
	 * identity security token, username + password credential, and default security entirely on a
	 * {@link Mutiny.StatelessSession}. Idempotent: when the creator user already exists it is a no-op.
	 */
	Uni<IInvolvedParty<?, ?>> createAdminAndCreatorUserForEnterprise(Mutiny.StatelessSession session, ISystems<?, ?> system, String adminUserName,
																	 @NotNull String adminPassword, UUID existingLocalKey);
}
