package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;

import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderClassifications;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderDefault;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderFlags;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;

import java.util.UUID;


/**
 * Query builder for Involved Parties.
 * Provides specialized methods for finding parties by identification and type.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the involved party entity
 */
public interface IInvolvedPartyQueryBuilder<J extends IInvolvedPartyQueryBuilder<J, E>, E extends IInvolvedParty<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J, E, UUID>,
		        IQueryBuilderEnterprise<J, E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{
	/**
	 * Finds a party by an identification type enum and value.
	 *
	 * @param idType         The identification type enum
	 * @param value          The identification value
	 * @param system         The system context
	 * @param identityTokens Security tokens
	 * @return This builder
	 */
	default J findByIdentificationType(Enum<?> idType, String value,ISystems<?,?> system, java.util.UUID... identityTokens)
	{
		return findByIdentificationType(idType.toString(), value,system, identityTokens);
	}

	/**
	 * Finds a party by an identification type string and value.
	 *
	 * @param idType         The identification type
	 * @param value          The identification value
	 * @param system         The system context
	 * @param identityTokens Security tokens
	 * @return This builder
	 */
	J findByIdentificationType(String idType, String value,ISystems<?,?> system,  java.util.UUID... identityTokens);

	/**
	 * Finds a party by type and value.
	 *
	 * @param idType         The type
	 * @param value          The value
	 * @param system         The system context
	 * @param identityTokens Security tokens
	 * @return A query builder for the party
	 */
	IInvolvedPartyQueryBuilder<?,?> findByType( String idType, String value, ISystems<?, ?> system,java.util.UUID... identityTokens);

	/**
	 * Finds all parties by type and value across history.
	 *
	 * @param idType         The type
	 * @param value          The value
	 * @param system         The system context
	 * @param identityTokens Security tokens
	 * @return A query builder for the party
	 */
	IInvolvedPartyQueryBuilder<?,?> findByTypeAll(String idType, String value, ISystems<?, ?> system, java.util.UUID... identityTokens);

	/**
	 * Filters by classification and value within a specific system.
	 *
	 * @param classification The classification
	 * @param value          The value
	 * @param system         The system context
	 * @return This builder
	 */
	J withClassification(IClassification<?, ?> classification, String value, ISystems<?,?> system);

}
