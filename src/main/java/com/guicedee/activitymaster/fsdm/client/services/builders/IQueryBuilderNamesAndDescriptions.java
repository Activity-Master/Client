package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

public interface IQueryBuilderNamesAndDescriptions<J extends IQueryBuilderNamesAndDescriptions<J, E, I>, E extends IWarehouseBaseTable<E, J, I>
		, I extends UUID>
		extends IQueryBuilderClassifications<J, E, I>
{
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(String name)
	{
		where(this.<E, String>getAttribute("name"), Equals, name);
		return (J) this;
	}
	
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(Enum<?> name)
	{
		where(this.<E, String>getAttribute("name"), Equals, name.toString());
		return (J) this;
	}
	
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withDescription(String name)
	{
		where(this.<E, String>getAttribute("description"), Equals, name);
		return (J) this;
	}
	
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withDescriptionLike(String name)
	{
		where(this.<E, String>getAttribute("description"), Like, "%" + name + "%");
		return (J) this;
	}
	
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(String... name)
	{
		where(this.<E, String>getAttribute("name"), InList, name);
		return (J) this;
	}
	
	@SuppressWarnings("unchecked")
	@jakarta.validation.constraints.NotNull
	default J withName(Collection<String> name)
	{
		where(this.<E, String>getAttribute("name"), InList, name);
		return (J) this;
	}
}
