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
 * An actual value for a relationship
 *
 * @param <P> The primary source
 * @param <S> The secondary source
 */
@SuppressWarnings("unused")
public interface IRelationshipValue<
		P extends IWarehouseBaseTable<?, ?, ?>,
		S extends IWarehouseBaseTable<?, ?, ?>,
		J extends IRelationshipValue<P,S,J>
		>
		extends IContainsClassifications<J>
{
	void setValue(String value);

	String getValue();

	default Integer getValueAsNumber()
	{
		return Integer.parseInt(getValue());
	}

	default Long getValueAsLong()
	{
		return Long.parseLong(getValue());
	}

	default Boolean getValueAsBoolean()
	{
		return Boolean.parseBoolean(getValue());
	}

	default BigDecimal getValueAsBigDecimal()
	{
		return BigDecimal.valueOf(getValueAsDouble());
	}

	default Double getValueAsDouble()
	{
		return Double.parseDouble(getValue());
	}

	default UUID getValueAsUUID()
	{
		return UUID.fromString(getValue());
	}

	/**
	 * The left hand side of the relationship
	 *
	 * @return
	 */
	P getPrimary();

	/**
	 * The right hand side of the relationship
	 *
	 * @return
	 */
	S getSecondary();

	default Uni<IRelationshipValue<P, S,?>> expire(Mutiny.Session session, Duration duration, UUID... identityToken)
	{
		IWarehouseBaseTable tableForClassification = (IWarehouseBaseTable) this;
		tableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow())
		                                                       .plus(duration));
		return tableForClassification.builder(session).update();
	}

	default Uni<IRelationshipValue<P, S,?>> expire(Mutiny.Session session, UUID... identityToken)
	{
		return expire(session, Duration.ZERO);
	}

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
				return tableForClassification.update(session, system, identityToken);
			});
	}

	default Uni<IRelationshipValue<P, S,?>> remove(Mutiny.Session session, Duration duration, ISystems<?,?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, ?,?,?,?> tableForClassification = (IWarehouseRelationshipTable<?, ?, ?, ?, ?,?>) this;
		tableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow())
			                                                       .plus(duration));
		IActiveFlagService<?> flagService = get(IActiveFlagService.class);

		return (Uni) flagService.getDeletedFlag(session, system.getEnterprise())
			.onItem().transformToUni(deletedFlag -> {
				tableForClassification.setActiveFlagID(deletedFlag);
				return tableForClassification.update(session, system, identityToken);
			});
	}

	default Uni<IRelationshipValue<P, S,?>> archive(Mutiny.Session session, ISystems<?,?> system, UUID... identityToken)
	{
		return archive(session, Duration.ZERO,system, identityToken);
	}

	default Uni<IRelationshipValue<P, S,?>> update(Mutiny.Session session, ISystems<?,?> originatingSystem, UUID... identityToken)
	{
		var tableForClassification = (IWarehouseBaseTable) this;
		return tableForClassification.builder(session).update();
	}

}
