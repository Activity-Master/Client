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
	Uni<IInvolvedParty<?, ?>> findByUsername(Mutiny.Session session, String username, ISystems<?, ?> system, UUID... identityToken);

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
	Uni<IInvolvedParty<?, ?>> findByUsernameAndPassword(Mutiny.Session session, String username, String password, ISystems<?, ?> system, boolean throwForNoUser, UUID... identityToken);

	/**
	 * Retrieves all users for a system.
	 *
	 * @param session        The Mutiny session to use
	 * @param system         The system performing the search
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting a list of all users
	 */
	Uni<List<IInvolvedParty<?, ?>>> getAllUsers(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Adds or updates a username and password for an involved party.
	 *
	 * @param session        The Mutiny session to use
	 * @param username       The username
	 * @param password       The password
	 * @param involvedParty  The involved party to update
	 * @param system         The system performing the operation
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting the updated involved party
	 */
	Uni<IInvolvedParty<?, ?>> addUpdateUsernamePassword(Mutiny.Session session, String username, String password, IInvolvedParty<?, ?> involvedParty, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Checks if a username already exists.
	 *
	 * @param session        The Mutiny session to use
	 * @param username       The username to check
	 * @param system         The system performing the check
	 * @param identityToken  Optional security identity tokens
	 * @return A Uni emitting true if the username exists, false otherwise
	 */
	Uni<Boolean> doesUsernameExist(Mutiny.Session session, String username, ISystems<?, ?> system, UUID... identityToken);

	/**
	 * Creates an administrator and creator user for an enterprise.
	 *
	 * @param session         The Mutiny session to use
	 * @param system          The system performing the operation
	 * @param adminUserName   The administrator username
	 * @param adminPassword   The administrator password
	 * @param existingLocalKey An optional existing key to use
	 * @return A Uni emitting the created administrator user
	 */
	Uni<IInvolvedParty<?, ?>> createAdminAndCreatorUserForEnterprise(Mutiny.Session session, ISystems<?, ?> system, String adminUserName,
																	 @NotNull String adminPassword, UUID existingLocalKey);
}
