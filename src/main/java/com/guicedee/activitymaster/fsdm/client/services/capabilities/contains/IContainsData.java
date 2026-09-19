package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


/**
 * Specifies that this object can return a byte[]
 *
 */
public interface IContainsData<J extends IContainsData<J>>
{
	/** Stateless variant of {@link #getData(Mutiny.StatelessSession, UUID...)}. */
	Uni<byte[]> getData(Mutiny.StatelessSession session, UUID... identityToken);
}
