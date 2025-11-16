package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface IContainsRowRecordInformation<J extends IContainsRowRecordInformation<J>>
{
	@NotNull
	UUID getOriginalSourceSystemUniqueID();

	J setOriginalSourceSystemUniqueID(@NotNull UUID originalSourceSystemUniqueID);

	UUID getOriginalSourceSystemID();

	J setOriginalSourceSystemID(UUID originalSourceSystemID);
}
