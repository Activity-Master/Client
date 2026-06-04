package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

/**
 * Query builder interface for entities with Name and Description fields.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier
 */
public interface IQueryBuilderNamesAndDescriptions<J extends IQueryBuilderNamesAndDescriptions<J, E, I>, E extends IWarehouseBaseTable<E, J, I>
		, I extends UUID>
		extends IQueryBuilderClassifications<J, E, I>
{
	/**
	 * Adds a filter for the 'name' field using equality.
	 *
	 * @param name The name to match
	 * @return This builder
	 */
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(String name)
	{
		where(this.<E, String>getAttribute("name"), Equals, name);
		return (J) this;
	}

	/**
	 * Adds a filter for the 'name' field using an enum's string representation.
	 *
	 * @param name The enum constant whose name to match
	 * @return This builder
	 */
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(Enum<?> name)
	{
		where(this.<E, String>getAttribute("name"), Equals, name.toString());
		return (J) this;
	}

	/**
	 * Adds a filter for the 'description' field using equality.
	 *
	 * @param name The description to match
	 * @return This builder
	 */
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withDescription(String name)
	{
		where(this.<E, String>getAttribute("description"), Equals, name);
		return (J) this;
	}

	/**
	 * Adds a filter for the 'description' field using a LIKE clause.
	 *
	 * @param name The partial description to match
	 * @return This builder
	 */
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withDescriptionLike(String name)
	{
		where(this.<E, String>getAttribute("description"), Like, "%" + name + "%");
		return (J) this;
	}

	/**
	 * Adds a filter for the 'name' field using an IN list of strings.
	 *
	 * @param name The names to match
	 * @return This builder
	 */
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(String... name)
	{
		where(this.<E, String>getAttribute("name"), InList, name);
		return (J) this;
	}

	/**
	 * Adds a filter for the 'name' field using a collection of strings.
	 *
	 * @param name The names to match
	 * @return This builder
	 */
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(Collection<String> name)
	{
		where(this.<E, String>getAttribute("name"), InList, name);
		return (J) this;
	}
}
