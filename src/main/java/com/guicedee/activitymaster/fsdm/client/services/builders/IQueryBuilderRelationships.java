package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import jakarta.persistence.metamodel.Attribute;

import java.io.Serializable;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

/**
 * Query builder interface for entities representing relationships between two other entities.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity (the relationship itself)
 * @param <P> The type of the primary (parent) entity in the relationship
 * @param <S> The type of the secondary (child) entity in the relationship
 * @param <I> The type of the entity identifier
 */
public interface IQueryBuilderRelationships<J extends IQueryBuilderRelationships<J, E, P, S, I>,
		E extends IWarehouseBaseTable<E, J, I>,
		P extends IWarehouseBaseTable<?, ?, ?>,
		S extends IWarehouseBaseTable<?, ?, ?>,
		I extends UUID>
		extends IQueryBuilderFlags<J, E, I>,
		        IQueryBuilderValues<J, E, I>,
		        IQueryBuilderEnterprise<J,E,I>,
		        IQueryBuilderSecurity<J,E,I>,
		        IQueryBuilderDefault<J,E,I>,
		        IQueryBuilderClassifications<J,E,I>
{
	/**
	 * Returns the metamodel attribute for the primary entity in this relationship.
	 *
	 * @return The primary attribute
	 */
	Attribute<?, P> getPrimaryAttribute();

	/**
	 * Returns the metamodel attribute for the secondary entity in this relationship.
	 *
	 * @return The secondary attribute
	 */
	Attribute<?, S> getSecondaryAttribute();

	/**
	 * Filters the query to find a specific link between a parent and child entity with an optional value.
	 *
	 * @param parent The parent entity
	 * @param child  The child entity
	 * @param value  The value associated with the link
	 * @return This builder
	 */
	@jakarta.validation.constraints.NotNull
	default J findLink(P parent, S child, String value)
	{
 	var primaryAttr = getPrimaryAttribute();
		if (parent != null && parent.getId() != null && primaryAttr != null)
		{
			where(primaryAttr, Equals, parent);
		}
		var secondaryAttr = getSecondaryAttribute();
		if (child != null && child.getId() != null && secondaryAttr != null)
		{
			where(secondaryAttr, Equals, child);
		}
		if (value != null)
		{
			withValue(Equals,value);
		}
		return (J) this;
	}
}
