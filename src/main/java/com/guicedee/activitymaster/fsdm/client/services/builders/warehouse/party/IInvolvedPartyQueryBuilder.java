package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;

import java.util.UUID;


public interface IInvolvedPartyQueryBuilder<J extends IInvolvedPartyQueryBuilder<J, E>, E extends IInvolvedParty<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J, E, UUID>,
		        IQueryBuilderEnterprise<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{
	default J findByIdentificationType(Enum<?> idType, String value,ISystems<?,?> system, java.util.UUID... identityTokens)
	{
		return findByIdentificationType(idType.toString(), value,system, identityTokens);
	}
	
	J findByIdentificationType(String idType, String value,ISystems<?,?> system,  java.util.UUID... identityTokens);
	
	IInvolvedPartyQueryBuilder<?,?> findByType( String idType, String value, ISystems<?, ?> system,java.util.UUID... identityTokens);
	
	IInvolvedPartyQueryBuilder<?,?> findByTypeAll(String idType, String value, ISystems<?, ?> system, java.util.UUID... identityTokens);
	
	J withClassification(IClassification<?, ?> classification, String value, ISystems<?,?> system);
	
	
	
}
