package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"unused", "DuplicatedCode"})
public interface IManageRuleTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
	private String getResourceItemsRelationshipTable()
	{
		String className = getClass().getCanonicalName() + "XRulesType";
		return className;
	}

	private Class<? extends IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>> getRuleTypeRelationshipClass()
	{
		String joinTableName = getResourceItemsRelationshipTable();
		try
		{
			//noinspection unchecked
			return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find ruleType linked class - " + joinTableName, e);
		}
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> findRulesTypes(Mutiny.Session session, String classificationName, String rulesType, String value, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
		IRulesService<?> rulesItemService = get(IRulesService.class);

		return rulesItemService.findRulesTypes(session, rulesType, system, identityToken)
			.chain(ruleType -> tableForClassification.builder(session)
				.findLink((J) this, ruleType, value)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.get()
				.map(result -> {
					if (result == null) {
						throw new NoSuchElementException("Rules type not found");
					}
					return (IRelationshipValue<J, IRulesType<?, ?>, ?>) result;
				})
			);
	}

	@SuppressWarnings("unchecked")
	default Uni<List<IRelationshipValue<J, IRulesType<?, ?>, ?>>> findRulesTypesAll(Mutiny.Session session, String classificationName, String rulesType, String value, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
		IRulesService<?> rulesItemService = get(IRulesService.class);

		if (rulesType == null)
		{
			return tableForClassification.builder(session)
				.findLink((J) this, null, value)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.getAll()
				.map(list -> (List<IRelationshipValue<J, IRulesType<?, ?>, ?>>) list);
		}

		return rulesItemService.findRulesTypes(session, rulesType, system, identityToken)
			.chain(ruleType -> tableForClassification.builder(session)
				.findLink((J) this, ruleType, value)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.getAll()
				.map(list -> (List<IRelationshipValue<J, IRulesType<?, ?>, ?>>) list)
			);
	}

	default Uni<Boolean> hasRuleTypes(Mutiny.Session session, String classificationName, String ruleTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		return numberOfRuleTypes(session, classificationName, ruleTypeName, system, identityToken)
			.map(count -> count > 0);
	}

	@SuppressWarnings("unchecked")
	default Uni<Long> numberOfRuleTypes(Mutiny.Session session, String classificationName, String ruleTypeName, ISystems<?, ?> system, UUID... identityToken)
	{
		IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
		IRulesService<?> rulesItemService = get(IRulesService.class);

		return rulesItemService.findRulesTypes(session, ruleTypeName, system, identityToken)
			.chain(ruleType -> tableForClassification.builder(session)
				.findLink((J) this, ruleType, null)
				.inActiveRange()
				.withClassification(classificationName, system)
				.inDateRange()
				.canRead(system, identityToken)
				.getCount()
			);
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addRuleTypes(Mutiny.Session session, String rulesType, String value, String classificationName, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
			IRulesService<?> rulesItemService = get(IRulesService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;

			// Sequential chain instead of parallel operations
			return rulesItemService.findRulesTypes(session, rulesType, system, identityToken)
				.chain(ruleType -> classificationService.find(session, finalClassificationNameCopy, system, identityToken)
					.map(classification -> {
						tableForClassification.setEnterpriseID(system.getEnterpriseID());
						tableForClassification.setValue(value);
						tableForClassification.setSystemID(system);
						tableForClassification.setClassificationID(classification);
						tableForClassification.setOriginalSourceSystemID(system.getId());
						tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
						tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
						tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
						tableForClassification.setActiveFlagID(system.getActiveFlagID());
						configureRuleTypeLinkValue(tableForClassification, (J) this, ruleType, classification, value, system.getEnterpriseID());

						return tableForClassification;
					})
				)
				.chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
				.chain(table -> 
					// Chain the createDefaultSecurity operation properly
					table.createDefaultSecurity(session, system, identityToken)
						.onFailure().invoke(error -> {
							// Log error but continue
							System.err.println("Error in createDefaultSecurity: " + error.getMessage());
						})
						.map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) table)
				);
	}

	@SuppressWarnings("rawtypes")
	void configureRuleTypeLinkValue(IWarehouseRelationshipTable linkTable, J primary, IRulesType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);


	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addOrReuseRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
			IRulesService<?> rulesItemService = get(IRulesService.class);

			// Prepare classification name
			final String finalClassificationName = Strings.isNullOrEmpty(classificationName) 
					? DefaultClassifications.NoClassification.toString() 
					: classificationName;

			// First get the rule type
			return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
				.onItem().transformToUni(ruleType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, ruleType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationName, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.onFailure(NoResultException.class)
						.recoverWithUni(() -> {
							return (Uni) addRuleTypes(session, rulesTypeName, value, finalClassificationName, system, identityToken);
						})
						.chain(result -> {
							// Cast the result to the correct type and return it
							return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) result);
						});
				});
	}

	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> addOrUpdateRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
			IRulesService<?> rulesItemService = get(IRulesService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			
			// Sequential chain instead of parallel operations
			return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
				.chain(ruleType -> classificationService.find(session, finalClassificationNameCopy, system, identityToken)
					.chain(classification -> {
						// Create a query to find the existing relationship
						return tableForClassification.builder(session)
							.findLink((J) this, ruleType, searchValue)
							.inActiveRange()
							.withClassification(finalClassificationNameCopy, system)
							.inDateRange()
							.canRead(system, identityToken)
							.get()
							.onFailure(NoResultException.class)
							.recoverWithUni(() -> {
								tableForClassification.setEnterpriseID(system.getEnterpriseID());
								tableForClassification.setValue(value);
								tableForClassification.setSystemID(system);
								tableForClassification.setOriginalSourceSystemID(system.getId());
								tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
								tableForClassification.setActiveFlagID(system.getActiveFlagID());
								configureRuleTypeLinkValue(tableForClassification, (J) this, ruleType, classification, value, system.getEnterpriseID());

								return (Uni) Uni.createFrom().item(tableForClassification)
									.chain(table -> {
										return session.persist(table).replaceWith(Uni.createFrom().item(table));
									})
									.chain(table -> {
										// Chain the createDefaultSecurity operation properly
										return table.createDefaultSecurity(session, system, identityToken)
											.onFailure().invoke(error -> {
												// Log error but continue
												System.err.println("Error in createDefaultSecurity: " + error.getMessage());
											})
											.map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) table);
									});
							})
							.chain(result -> {
								// Cast the result to the correct type
								IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable = 
									(IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

								// If the value is the same, return the existing relation
								if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
									return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
								}

								// Otherwise, update the relation
								ISystems<?, ?> originalSystem = existingTable.getSystemID();
								IActiveFlagService<?> flagService = get(IActiveFlagService.class);

								return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
									.chain(archivedFlag -> {
										existingTable.setActiveFlagID(archivedFlag);
										existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
										return session.merge(existingTable);
									})
									.chain(() -> {
										IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getRuleTypeRelationshipClass());
										newTableForClassification.setId(null);
										newTableForClassification.setSystemID(system);
										newTableForClassification.setOriginalSourceSystemID(originalSystem.getId());
										newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
										newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
										newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
										newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
										newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));

										return flagService.getActiveFlag(session, originalSystem.getEnterpriseID(), identityToken)
											.map(activeFlag -> {
												newTableForClassification.setActiveFlagID(activeFlag);
												newTableForClassification.setValue(value);
												newTableForClassification.setEnterpriseID(system.getEnterpriseID());
												configureRuleTypeLinkValue(newTableForClassification, (J) this, ruleType, classification, value, system.getEnterpriseID());
												return newTableForClassification;
											});
									})
									.chain(newTable -> {
										return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
									})
									.chain(newTable -> {
										// Chain the createDefaultSecurity operation properly
										return newTable.createDefaultSecurity(session, originalSystem, identityToken)
											.onFailure().invoke(error -> {
												// Log error but continue
												System.err.println("Error in createDefaultSecurity: " + error.getMessage());
											})
											.map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) newTable);
									});
							});
					})
				);
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> updateRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
			IRulesService<?> rulesItemService = get(IRulesService.class);
			IClassificationService<?> classificationService = get(IClassificationService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			
			// Sequential chain instead of parallel operations
			return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
				.chain(ruleType -> classificationService.find(session, finalClassificationNameCopy, system, identityToken)
					.chain(classification -> {
						// Create a query to find the existing relationship
						return tableForClassification.builder(session)
							.findLink((J) this, ruleType, searchValue)
							.inActiveRange()
							.withClassification(finalClassificationNameCopy, system)
							.inDateRange()
							.canRead(system, identityToken)
							.get()
							.chain(result -> {
								// If result is null, do nothing
								if (result == null) {
									return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
								}

								// Cast the result to the correct type
								IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable = 
										(IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

								// If the value is the same, return the existing relation
								if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
									return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
								}

								// Otherwise, update the relation
								ISystems<?, ?> originalSystem = existingTable.getSystemID();
								IActiveFlagService<?> flagService = get(IActiveFlagService.class);

								return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
										.chain(archivedFlag -> {
											existingTable.setActiveFlagID(archivedFlag);
											existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
											return session.merge(existingTable);
										})
										.chain(() -> {
											IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getRuleTypeRelationshipClass());
											newTableForClassification.setId(null);
											newTableForClassification.setSystemID(system);
											newTableForClassification.setOriginalSourceSystemID(originalSystem.getId());
											newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
											newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
											newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
											newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
											newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));

											return flagService.getActiveFlag(session, originalSystem.getEnterpriseID(), identityToken)
													.map(activeFlag -> {
														newTableForClassification.setActiveFlagID(activeFlag);
														newTableForClassification.setValue(value);
														newTableForClassification.setEnterpriseID(system.getEnterpriseID());
														configureRuleTypeLinkValue(newTableForClassification, (J) this, ruleType, classification, value, system.getEnterpriseID());
														return newTableForClassification;
													});
										})
										.chain(newTable -> {
											return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
										})
										.chain(newTable -> {
											// Chain the createDefaultSecurity operation properly
											return newTable.createDefaultSecurity(session, originalSystem, identityToken)
												.onFailure().invoke(error -> {
													// Log error but continue
													System.err.println("Error in createDefaultSecurity: " + error.getMessage());
												})
												.map(v -> (IRelationshipValue<J, IRulesType<?, ?>, ?>) newTable);
										});
							});
					})
				);
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> expireRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
			IRulesService<?> rulesItemService = get(IRulesService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
				.chain(ruleType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, ruleType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationNameCopy, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable = 
									(IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
							}

							// Otherwise, expire the relation
							existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
							return session.merge(existingTable);
						});
				});
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> archiveRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
			IRulesService<?> rulesItemService = get(IRulesService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
				.chain(ruleType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, ruleType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationNameCopy, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable = 
									(IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
							}

							// Otherwise, archive the relation
							IActiveFlagService<?> flagService = get(IActiveFlagService.class);
							return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
									.chain(archivedFlag -> {
										existingTable.setActiveFlagID(archivedFlag);
										existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
										return session.merge(existingTable);
									});
						});
				});
	}

	@SuppressWarnings("unchecked")
	default Uni<IRelationshipValue<J, IRulesType<?, ?>, ?>> removeRuleTypes(Mutiny.Session session, String rulesTypeName, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
	{
			IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> tableForClassification = get(getRuleTypeRelationshipClass());
			IRulesService<?> rulesItemService = get(IRulesService.class);

			// Prepare classification name
			String finalClassificationName = classificationName;
			if (Strings.isNullOrEmpty(finalClassificationName))
			{
				finalClassificationName = DefaultClassifications.NoClassification.toString();
			}

			final String finalClassificationNameCopy = finalClassificationName;
			return rulesItemService.findRulesTypes(session, rulesTypeName, system, identityToken)
				.chain(ruleType -> {
					// Create a query to find the existing relationship
					return tableForClassification.builder(session)
						.findLink((J) this, ruleType, searchValue)
						.inActiveRange()
						.withClassification(finalClassificationNameCopy, system)
						.inDateRange()
						.canRead(system, identityToken)
						.get()
						.chain(result -> {
							// If result is null, do nothing
							if (result == null) {
								return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) tableForClassification);
							}

							// Cast the result to the correct type
							IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?> existingTable = 
									(IWarehouseRelationshipTable<?, ?, J, IRulesType<?, ?>, java.util.UUID, ?>) result;

							// If the value is the same, return the existing relation
							if (Strings.nullToEmpty(value).equals(existingTable.getValue())) {
								return Uni.createFrom().item((IRelationshipValue<J, IRulesType<?, ?>, ?>) existingTable);
							}

							// Otherwise, remove the relation
							IActiveFlagService<?> flagService = get(IActiveFlagService.class);
							return flagService.getDeletedFlag(session, system.getEnterpriseID(), identityToken)
									.chain(deletedFlag -> {
										existingTable.setActiveFlagID(deletedFlag);
										existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
										return session.merge(existingTable);
									});
						});
				});
	}
}

