package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseSecurityTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Logger;


public interface IQueryBuilderSecurity<J extends IQueryBuilderSecurity<J, E, I>,
		E extends IWarehouseBaseTable<E, J, I>,
		I extends UUID>
	extends IQueryBuilderDefault<J,E,I>
{
	
	private String getSecuritiessRelationshipTable()
	{
		String className = getEntity().getClass().getCanonicalName().replace("QueryBuilder","") + "SecurityToken";
		return className;
	}
	
	private Class<? extends IWarehouseSecurityTable<?, ?,?>> getSecuritiesRelationshipClass()
	{
		String joinTableName = getSecuritiessRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseSecurityTable<?, ?,?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find classification linked class - " + joinTableName, e);
		}
	}
	
	@NotNull
	default J canRead(ISystems<?,?> system, UUID... identityToken)
	{
		// If no identity tokens provided, skip security filtering (useful for tests and public reads)
		if (identityToken == null || identityToken.length == 0)
		{
      //todo this must get locked down always expect a populated system calling and the party identity token
			//Logger.getLogger("Security Check").warning("Skipping security check due to no security identity tokens provided.");
			return (J)this;
		}
		
		Class<? extends IWarehouseSecurityTable<?, ?,?>> securityRelationshipClass = getSecuritiesRelationshipClass();
		
	//	J securityJoin = join(getAttribute("securities"));
		
		
		return (J)this;
	}
	
}
