package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;

import java.util.UUID;

public interface IContainsId
        <J extends IContainsId<J>>
{
    UUID getId();
    J setId(UUID uuid);
}
