package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
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

	/**
	 * Updates the data for the given item
	 *
		* @deprecated Use IResourceItemService
		*
	 * @param session
	 * @param data    The data of the item
	 * @return A Uni that completes when the update is done
	 */
	//Uni<Void> updateData(Mutiny.Session session, byte[] data, ISystems<?,?> system, UUID... identityToken);

	/**
	 * Updates the data for the given item and keeps history
	 *
	 * @param session
	 * @param data          The data of the item
	 * @param system        The system performing the update
	 * @param identityToken The identity token
	 * @return A Uni that completes when the update is done
	 */
	Uni<Void> updateAndKeepHistoryData(Mutiny.Session session, byte[] data, ISystems<?,?> system, UUID... identityToken);
}
