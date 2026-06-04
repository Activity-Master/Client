package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.entityassist.enumerations.Operand;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

/**
 * Query builder interface for entities with a 'value' field.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier
 */
public interface IQueryBuilderValues<J extends IQueryBuilderValues<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>, I extends UUID>
		extends IQueryBuilderFlags<J, E, I>

{
	/**
	 * Adds a filter for the 'value' field using equality.
	 *
	 * @param value The value to match
	 * @return This builder
	 */
	@jakarta.validation.constraints.NotNull
	default J withValue(String value)
	{
		return withValue(Equals, value);
	}

	/**
	 * Adds a filter for the 'value' field using a specified operand.
	 *
	 * @param operand The operand to use (e.g., Equals, Like)
	 * @param value   The value to match
	 * @return This builder
	 */
	@jakarta.validation.constraints.NotNull
	default J withValue(Operand operand, String value)
	{
		if (value != null)
		{
			where(this.<E, String>getAttribute("value"), operand, value);
		}
		//noinspection unchecked
		return (J) this;
	}

	/**
	 * Adds a filter for the 'value' field using an IN list of values.
	 *
	 * @param values The values to match
	 * @return This builder
	 */
	@jakarta.validation.constraints.NotNull
	default J withValues(String... values)
	{
		if (values.length > 0)
		{
			where(this.<E, String>getAttribute("value"), Operand.InList, values);
		}
		//noinspection unchecked
		return (J) this;
	}

	/**
	 * Adds a filter for the 'value' field using a collection of values.
	 *
	 * @param values The values to match
	 * @return This builder
	 */
	@jakarta.validation.constraints.NotNull
	default J withValues(Collection<String> values)
	{
		if (values.size() > 0)
		{
			where(this.<E, String>getAttribute("value"), Operand.InList, values);
		}
		//noinspection unchecked
		return (J) this;
	}
}
