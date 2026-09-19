package com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events;

import com.entityassist.querybuilder.QueryBuilder;
import com.guicedee.activitymaster.fsdm.client.services.builders.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.metamodel.Attribute;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;


/**
 * Query builder for Events.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the event entity
 */
public interface IEventQueryBuilder<J extends IEventQueryBuilder<J, E>, E extends IEvent<E, J>>
		extends IQueryBuilderDefault<J, E, UUID>,
		        IQueryBuilderFlags<J,E, UUID>,
		        IQueryBuilderEnterprise<J,E, UUID>,
		        IQueryBuilderClassifications<J,E, UUID>
{

	private Class<? extends IWarehouseRelationshipTable<?, ?, E, IEventType<?, ?>, java.util.UUID, ?>> getEventTypeRelationshipClass()
	{
		String myTableName = getClass().getCanonicalName();
		String joinTableName = myTableName + "XEventType";
		try
		{
			return (Class<? extends IWarehouseRelationshipTable<?, ?, E, IEventType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			return null;
		}
	}

	// ---- Stateless (Mutiny.StatelessSession) twins. The relationship builder is session-polymorphic; the
	// join wiring carries no session, so these mirror the managed convenience filters verbatim. ----

	default Uni<J> hasEventType(Mutiny.StatelessSession session, IClassification<?,?> classification, String value, UUID... identityToken)
	{
		ISystems<?,?> systemID = classification.getSystemID();
		return hasEventType(session, classification.getName(), value, systemID, identityToken);
	}

	default Uni<J> hasEventType(Mutiny.StatelessSession session, String classification, ISystems<?,?> system, UUID... identityToken)
	{
		return hasEventType(session, classification, null, system, identityToken);
	}

	default Uni<J> hasEventType(Mutiny.StatelessSession session, String classificationName, String value, ISystems<?,?> system, UUID... identityToken)
	{
		Class<? extends IWarehouseRelationshipTable<?, ?, E, IEventType<?, ?>, java.util.UUID, ?>> relationshipTable = getEventTypeRelationshipClass();
		IWarehouseRelationshipTable<?, ?, E, IEventType<?, ?>, java.util.UUID, ?> instance = com.guicedee.client.IGuiceContext.get(relationshipTable);
		IQueryBuilderRelationships qbr
				= instance.builder(session);

		qbr.withClassification(classificationName, system);
		qbr.inActiveRange();
		qbr.inDateRange();
		qbr.withValue(value);

		Attribute<E,IQueryBuilderRelationships> joinColumn = getAttribute("eventTypes");
		join(joinColumn, (QueryBuilder) qbr);

		//noinspection unchecked
		return Uni.createFrom().item((J) this);
	}

}
