package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.capabilities.contains.IContainsClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

/**
 * Represents an actual value for a relationship between two entities.
 * Relationships are stored in a primary-secondary structure with an associated text value and classifications.
 *
 * @param <P> The primary source entity type
 * @param <S> The secondary source entity type
 * @param <J> The concrete type of the relationship value
 */
@SuppressWarnings("unused")
public interface IRelationshipValue<
		P extends IWarehouseBaseTable<?, ?, ?>,
		S extends IWarehouseBaseTable<?, ?, ?>,
		J extends IRelationshipValue<P,S,J>
		>
		extends IContainsClassifications<J>
{
	/**
	 * Sets the text value for the relationship.
	 *
	 * @param value The value to set
	 */
	void setValue(String value);

	/**
	 * Gets the text value of the relationship.
	 *
	 * @return The relationship value
	 */
	String getValue();

	/**
	 * Gets the value parsed as an Integer.
	 *
	 * @return The integer value
	 */
	default Integer getValueAsNumber()
	{
		return Integer.parseInt(getValue());
	}

	/**
	 * Gets the value parsed as a Long.
	 *
	 * @return The long value
	 */
	default Long getValueAsLong()
	{
		return Long.parseLong(getValue());
	}

	/**
	 * Gets the value parsed as a Boolean.
	 *
	 * @return The boolean value
	 */
	default Boolean getValueAsBoolean()
	{
		return Boolean.parseBoolean(getValue());
	}

	/**
	 * Gets the value parsed as a BigDecimal.
	 *
	 * @return The BigDecimal value
	 */
	default BigDecimal getValueAsBigDecimal()
	{
		return BigDecimal.valueOf(getValueAsDouble());
	}

	/**
	 * Gets the value parsed as a Double.
	 *
	 * @return The double value
	 */
	default Double getValueAsDouble()
	{
		return Double.parseDouble(getValue());
	}

	/**
	 * Gets the value parsed as a UUID.
	 *
	 * @return The UUID value
	 */
	default UUID getValueAsUUID()
	{
		return UUID.fromString(getValue());
	}

	/**
	 * Gets the primary entity (LHS) of the relationship.
	 *
	 * @return The primary entity
	 */
	P getPrimary();

	/**
	 * Gets the secondary entity (RHS) of the relationship.
	 *
	 * @return The secondary entity
	 */
	S getSecondary();

	/**
	 * Expires the relationship after a certain duration.
	 *
	 * @param session       The Mutiny session to use
	 * @param duration      The duration until expiration
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the updated relationship value
	 */
	default Uni<IRelationshipValue<P, S,?>> expire(Mutiny.Session session, Duration duration, UUID... identityToken)
	{
		IWarehouseBaseTable tableForClassification = (IWarehouseBaseTable) this;
		tableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow())
		                                                       .plus(duration));
		return tableForClassification.builder(session).update();
	}

	/**
	 * Immediately expires the relationship.
	 *
	 * @param session       The Mutiny session to use
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the updated relationship value
	 */
	default Uni<IRelationshipValue<P, S,?>> expire(Mutiny.Session session, UUID... identityToken)
	{
		return expire(session, Duration.ZERO);
	}

	/**
	 * Archives the relationship after a certain duration.
	 * Sets the active flag to 'Archived'.
	 *
	 * @param session       The Mutiny session to use
	 * @param duration      The duration until archiving
	 * @param system        The system performing the operation
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the archived relationship value
	 */
	default Uni<IRelationshipValue<P, S,?>> archive(Mutiny.Session session, Duration duration, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, ?,?,?,?> tableForClassification = (IWarehouseRelationshipTable<?, ?, ?, ?, ?,?>) this;
		// Set the end date when archiving to close out the active range
		tableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow())
				.plus(duration));
		IActiveFlagService<?> flagService = get(IActiveFlagService.class);

		return (Uni) flagService.getArchivedFlag(session, system.getEnterprise())
			.onItem().transformToUni(archivedFlag -> {
				tableForClassification.setActiveFlagID(archivedFlag);
				// Detach first so update()'s merge is treated as an explicit update of a detached
				// instance. Under Hibernate Reactive with bytecode enhancement, mutating a still
				// managed entity and merging is a no-op (self-dirty-tracking is not flushed), so the
				// archive would silently not persist. This is a pure close (no following insert).
				session.detach(tableForClassification);
				return tableForClassification.update(session, system, identityToken);
			});
	}

	/**
	 * Removes (deletes) the relationship after a certain duration.
	 * Sets the active flag to 'Deleted'.
	 *
	 * @param session       The Mutiny session to use
	 * @param duration      The duration until removal
	 * @param system        The system performing the operation
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the removed relationship value
	 */
	default Uni<IRelationshipValue<P, S,?>> remove(Mutiny.Session session, Duration duration, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, ?,?,?,?> tableForClassification = (IWarehouseRelationshipTable<?, ?, ?, ?, ?,?>) this;
		tableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow())
			                                                       .plus(duration));
		IActiveFlagService<?> flagService = get(IActiveFlagService.class);

		return (Uni) flagService.getDeletedFlag(session, system.getEnterprise())
			.onItem().transformToUni(deletedFlag -> {
				tableForClassification.setActiveFlagID(deletedFlag);
				// See archive(): detach before update() so the close is actually flushed under
				// Hibernate Reactive bytecode enhancement (merging a managed entity is a no-op).
				session.detach(tableForClassification);
				return tableForClassification.update(session, system, identityToken);
			});
	}

	/**
	 * Immediately archives the relationship.
	 *
	 * @param session       The Mutiny session to use
	 * @param system        The system performing the operation
	 * @param identityToken Optional security identity tokens
	 * @return A Uni emitting the archived relationship value
	 */
	default Uni<IRelationshipValue<P, S,?>> archive(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken)
	{
		return archive(session, Duration.ZERO,system, identityToken);
	}

	/**
	 * Updates the relationship value in the database.
	 *
	 * @param session           The Mutiny session to use
	 * @param originatingSystem The system originating the update
	 * @param identityToken      Optional security identity tokens
	 * @return A Uni emitting the updated relationship value
	 */
	default Uni<IRelationshipValue<P, S,?>> update(Mutiny.Session session, ISystems<?,?> originatingSystem, UUID... identityToken)
	{
		var tableForClassification = (IWarehouseBaseTable) this;
		return tableForClassification.builder(session).update();
	}

}
