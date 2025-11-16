package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderNamesAndDescriptions;

import java.io.Serializable;
import java.util.UUID;

public interface ISecurityTokenQueryBuilder<J extends ISecurityTokenQueryBuilder<J,E>,E extends ISecurityToken<E,J>>
		extends IQueryBuilderNamesAndDescriptions<J, E, UUID>
{

}
