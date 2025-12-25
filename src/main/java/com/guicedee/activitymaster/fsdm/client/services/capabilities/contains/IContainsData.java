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
	/**
	 * Returns the data
	 * @return A Uni emitting the byte[] of data
	 */
	Uni<byte[]> getData(Mutiny.Session session, UUID... identityToken);
}
