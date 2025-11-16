package com.guicedee.activitymaster.fsdm.client.services;


import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products.IProduct;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRules;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.UUID;


public interface IRulesService<J extends IRulesService<J>>
{
	String RulesSystemName = "Rules System";

	Uni<IRules<?, ?>> createRules(Mutiny.Session session, String rulesType, String name, String description, ISystems<?,?> system, UUID... identityToken);

	Uni<IRules<?, ?>> createRules(Mutiny.Session session, String rulesType, UUID key, String name, String description, ISystems<?, ?> system, UUID... identityToken);

	Uni<IRules<?,?>> find(Mutiny.Session session, UUID identity);

	Uni<IRulesType<?,?>> findType(Mutiny.Session session, UUID identity);

	Uni<IRules<?,?>> findRules(Mutiny.Session session, String name, IEnterprise<?,?> enterprise, UUID... identityToken);

	Uni<IRules<?,?>> findRules(Mutiny.Session session, String productName, IClassification<?,?> classification, IEnterprise<?,?> enterprise, UUID... identityToken);

	default Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, Enum<?> rulesType, ISystems<?,?> system, UUID... identityToken)
	{
		return createRulesType(session, rulesType.toString(), system, identityToken);
	}

	Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, String rulesType, ISystems<?,?> system, UUID... identityToken);

	Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, String rulesType, String description, ISystems<?,?> system, UUID... identityToken);

	Uni<IRulesType<?,?>> createRulesType(Mutiny.Session session, String rulesType, UUID key, String description, ISystems<?, ?> system, UUID... identityToken);

	Uni<IRulesType<?,?>> findRulesTypes(Mutiny.Session session, String rulesType, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRulesType<?,?>>> findRulesTypes(Mutiny.Session session, String classifications, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRules<?,?>>> findByRulesTypes(Mutiny.Session session, IRulesType<?,?> rulesType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRulesType<?,?>>> findRuleTypesByRules(Mutiny.Session session, IRules<?,?> rulesType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRelationshipValue<IRules<?,?>,IRulesType<?,?>,?>>> findRuleTypeValuesByRules(Mutiny.Session session, IRules<?,?> rulesType, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRules<?,?>>> findRulesByProduct(Mutiny.Session session, IProduct<?,?> product, String classificationName, String value, ISystems<?,?> system, UUID... identityToken);

	Uni<List<IRelationshipValue<IRules<?,?>, IResourceItem<?,?>,?>>> findRulesByResourceItem(Mutiny.Session session, IResourceItem<?, ?> resourceItem, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken);
}
