package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.entityassist.enumerations.Operand;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

public interface IQueryBuilderValues<J extends IQueryBuilderValues<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>, I extends UUID>
		extends IQueryBuilderFlags<J, E, I>

{
	@jakarta.validation.constraints.NotNull
	default J withValue(String value)
	{
		return withValue(Equals, value);
	}
	
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
