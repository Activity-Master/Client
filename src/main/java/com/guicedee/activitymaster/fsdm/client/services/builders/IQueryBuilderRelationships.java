package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import jakarta.persistence.metamodel.Attribute;

import java.io.Serializable;
import java.util.UUID;

import static com.entityassist.enumerations.Operand.*;

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
	Attribute<?, P> getPrimaryAttribute();
	
	Attribute<?, S> getSecondaryAttribute();
	
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
