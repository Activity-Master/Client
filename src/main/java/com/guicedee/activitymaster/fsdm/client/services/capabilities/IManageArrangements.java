package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings("DuplicatedCode")
public interface IManageArrangements<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
    private String getArrangementsRelationshipTable()
    {
        String className = getClass().getCanonicalName() + "XArrangement";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?>> getArrangementRelationshipClass()
    {
        String joinTableName = getArrangementsRelationshipTable();
        try
        {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Cannot find arrangement linked class - " + joinTableName, e);
        }
    }

    void configureArrangementAddable(IWarehouseRelationshipTable linkTable, J primary, IArrangement<?, ?> secondary, IClassification<?, ?> classificationValue, String value, ISystems<?, ?> system);

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IArrangement<?, ?>, ?>> findArrangement(Mutiny.Session session, String classification, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IArrangement<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
                = relationshipTable.builder(session)
                          .findLink((J) this, null, null)
                          .inActiveRange()
                          .withClassification(classification, system)
                          .withValue(searchValue)
                          .inDateRange()
                          .withEnterprise(system.getEnterprise())
                          .canRead(system, identityToken)
                ;
        if (first)
        {
            queryBuilderRelationshipClassification.setMaxResults(1);
        }
        if (latest)
        {
            queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
        }

        // Use a different approach to handle the type compatibility issue
        return queryBuilderRelationshipClassification.get()
                       .onItem()
                       .ifNull()
                       .failWith(() -> new NoSuchElementException("Arrangement not found"))
                       .map(item -> {
                           // Explicit cast to handle type compatibility
                           return (IRelationshipValue<J, IArrangement<?, ?>, ?>) item;
                       });
    }

    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IArrangement<?, ?>, ?>>> findArrangementsAll(Mutiny.Session session, String classification, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IArrangement<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
                = relationshipTable.builder(session)
                          .findLink((J) this, null, null)
                          .inActiveRange()
                          .withClassification(classification, system)
                          .withValue(searchValue)
                          .inDateRange()
                          .withEnterprise(system.getEnterprise())
                          .canRead(system, identityToken)
                ;
        if (latest)
        {
            queryBuilderRelationshipClassification.orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"));
        }

        // Use a different approach to handle the type compatibility issue
        return queryBuilderRelationshipClassification.getAll()
                       .map(list -> {
                           // Explicit cast to handle type compatibility
                           return (List<IRelationshipValue<J, IArrangement<?, ?>, ?>>) list;
                       });
    }

    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfArrangements(Mutiny.Session session, String classificationValue, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> relationshipTable = get(getArrangementRelationshipClass());
        if (classificationValue == null)
        {
            classificationValue = DefaultClassifications.NoClassification.classificationValue();
        }
        final String finalClassificationValue = classificationValue;
        return relationshipTable.builder(session)
                       .findLink((J) this, null, value)
                       .withClassification(finalClassificationValue, system)
                       .inActiveRange()
                       .inDateRange()
                       .canRead(system, identityToken)
                       .getCount();
    }


    default Uni<Boolean> hasArrangements(Mutiny.Session session, String arrangementTypeName, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return numberOfArrangements(session, arrangementTypeName, searchValue, system, identityToken)
                       .map(count -> count > 0);
    }

    /// /@Transactional()
    default Uni<IRelationshipValue<J, IArrangement<?, ?>, ?>> addArrangement(Mutiny.Session session, IArrangement<?, ?> arrangement,
                                                                             String classificationName,
                                                                             String value,
                                                                             ISystems<?, ?> system,
                                                                             UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementRelationshipClass());
        IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);

        return classificationService.find(session, classificationName, system, identityToken)
                       .map(classification -> {
                           tableForClassification.setEnterpriseID(system.getEnterpriseID());
                           tableForClassification.setValue(Strings.nullToEmpty(value));
                           tableForClassification.setSystemID(system);
                           tableForClassification.setOriginalSourceSystemID(system.getId());
                           tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                           tableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
                           tableForClassification.setActiveFlagID(system.getActiveFlagID());
                           tableForClassification.setClassificationID(classification);

                           //noinspection unchecked
                           configureArrangementAddable(tableForClassification, (J) this,
                                   arrangement,
                                   classification, value, system);

                           return tableForClassification;
                       })
                       .chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
                       .chain(table -> {
                           // Start the createDefaultSecurity operation but don't wait for it to complete
                           table.createDefaultSecurity(session, system, identityToken);
                           // Return the table immediately without waiting for createDefaultSecurity to complete
                           return Uni.createFrom()
                                          .item((IRelationshipValue<J, IArrangement<?, ?>, ?>) table);
                       });
    }

    @SuppressWarnings("unchecked")
    ////@Transactional()
    default Uni<IRelationshipValue<J, IArrangement<?, ?>, ?>> addOrUpdateArrangement(Mutiny.Session session, String classificationValue,
                                                                                     IArrangement<?, ?> arrangementType,
                                                                                     String searchValue,
                                                                                     String storeValue,
                                                                                     ISystems<?, ?> system,
                                                                                     UUID... identityToken)
    {
        final IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> {
                           // Create a query to find the existing relationship
                           IQueryBuilderRelationships<?, ?, J, IArrangement<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
                                                                                                                   .findLink((J) this, null, null)
                                                                                                                   .withValue(searchValue)
                                                                                                                   .inActiveRange()
                                                                                                                   .inDateRange()
                                                                                                                   .withClassification(classificationValue, system)
                                   ;

                           // Get the result and handle it
                           return query.get()
                                          .onFailure(NoResultException.class)
                                          .recoverWithUni(() -> {
                                              return (Uni) addArrangement(session, arrangementType, classificationValue, storeValue, system, identityToken);
                                          })
                                          .chain(result -> {
                                              // Cast the result to the correct type
                                              IRelationshipValue<J, IArrangement<?, ?>, ?> existingRelation = (IRelationshipValue<J, IArrangement<?, ?>, ?>) result;

                                              // If the value is the same, return the existing relation
                                              if (Strings.nullToEmpty(storeValue)
                                                          .equals(existingRelation.getValue()))
                                              {
                                                  return Uni.createFrom()
                                                                 .item(existingRelation);
                                              }

                                              // Otherwise, update the relation
                                              final IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?>) result;
                                              IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                              return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
                                                             .chain(archivedFlag -> {
                                                                 existingTable.setActiveFlagID(archivedFlag);
                                                                 existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                                                 return session.merge(existingTable);
                                                             })
                                                             .chain(() -> {
                                                                 IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> newTableForClassification = get(getArrangementRelationshipClass());
                                                                 newTableForClassification.setId(null);
                                                                 newTableForClassification.setClassificationID(existingTable.getClassificationID());
                                                                 newTableForClassification.setSystemID(system);
                                                                 newTableForClassification.setOriginalSourceSystemID(existingTable.getId());
                                                                 newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
                                                                 newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                 newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                 newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                                                 newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));

                                                                 return flagService.getActiveFlag(session, system.getEnterpriseID(), identityToken)
                                                                                .map(activeFlag -> {
                                                                                    newTableForClassification.setActiveFlagID(activeFlag);
                                                                                    newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                                                    newTableForClassification.setEnterpriseID(system.getEnterpriseID());
                                                                                    configureArrangementAddable(newTableForClassification, existingTable.getPrimary(), existingTable.getSecondary(),
                                                                                            classification, storeValue, system);
                                                                                    return newTableForClassification;
                                                                                });
                                                             })
                                                             .chain(newTable -> {
                                                                 return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
                                                             })
                                                             .chain(newTable -> {
                                                                 newTable.createDefaultSecurity(session, system, identityToken);
                                                                 return Uni.createFrom()
                                                                                .item((IRelationshipValue<J, IArrangement<?, ?>, ?>) existingTable);
                                                             });
                                          });
                       });
    }

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IArrangement<?, ?>, ?>> addOrReuseArrangement(Mutiny.Session session, String classificationValue,
                                                                                    IArrangement<?, ?> arrangementType,
                                                                                    String searchValue,
                                                                                    ISystems<?, ?> system,
                                                                                    UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IArrangement<?, ?>, java.util.UUID, ?> tableForClassification = get(getArrangementRelationshipClass());

        // Create a query to find the existing relationship
        IQueryBuilderRelationships<?, ?, J, IArrangement<?, ?>, java.util.UUID> query = tableForClassification.builder(session)
                                                                                                .findLink((J) this, null, null)
                                                                                                .withValue(searchValue)
                                                                                                .inActiveRange()
                                                                                                .inDateRange()
                                                                                                .withClassification(classificationValue, system)
                ;

        // Get the result and handle it
        return query.get()
                       .onFailure(NoResultException.class)
                       .recoverWithUni(() -> {
                           return (Uni) addArrangement(session, arrangementType, classificationValue, searchValue, system, identityToken);
                       })
                       .chain(result -> {
                           // Cast the result to the correct type and return it
                           return Uni.createFrom()
                                          .item((IRelationshipValue<J, IArrangement<?, ?>, ?>) result);
                       });
    }

     // Convenience pass-throughs to ArrangementsService searches
     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByClassification(org.hibernate.reactive.mutiny.Mutiny.Session session, String classificationName, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByClassification(session, classificationName, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByClassification(org.hibernate.reactive.mutiny.Mutiny.Session session, String arrangementType, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?> withParent, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByClassification(session, arrangementType, withParent, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByClassificationGT(org.hibernate.reactive.mutiny.Mutiny.Session session, String arrangementType, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?> withParent, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByClassificationGT(session, arrangementType, withParent, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByClassificationGTE(org.hibernate.reactive.mutiny.Mutiny.Session session, String arrangementType, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?> withParent, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByClassificationGTE(session, arrangementType, withParent, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByClassificationLT(org.hibernate.reactive.mutiny.Mutiny.Session session, String arrangementType, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?> withParent, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByClassificationLT(session, arrangementType, withParent, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByClassificationLTE(org.hibernate.reactive.mutiny.Mutiny.Session session, String arrangementType, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?> withParent, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByClassificationLTE(session, arrangementType, withParent, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>> findArrangementByResourceItem(org.hibernate.reactive.mutiny.Mutiny.Session session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem<?, ?> resourceItem, String classificationName, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementByResourceItem(session, resourceItem, classificationName, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>> findArrangementByInvolvedParty(org.hibernate.reactive.mutiny.Mutiny.Session session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty<?, ?> involvedParty, String classificationName, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementByInvolvedParty(session, involvedParty, classificationName, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByInvolvedParty(org.hibernate.reactive.mutiny.Mutiny.Session session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty<?, ?> involvedParty, String classificationName, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByInvolvedParty(session, involvedParty, classificationName, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByInvolvedParty(org.hibernate.reactive.mutiny.Mutiny.Session session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty<?, ?> involvedParty, String classificationName, String value, java.time.LocalDateTime startDate, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByInvolvedParty(session, involvedParty, classificationName, value, startDate, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByInvolvedParty(org.hibernate.reactive.mutiny.Mutiny.Session session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty<?, ?> involvedParty, String classificationName, String value, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByInvolvedParty(session, involvedParty, classificationName, value, startDate, endDate, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?>>> findArrangementsByRulesType(org.hibernate.reactive.mutiny.Mutiny.Session session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules.IRulesType<?, ?> ruleType, String classificationName, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementsByRulesType(session, ruleType, classificationName, value, system, identityToken);
     }

     default io.smallrye.mutiny.Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty<?, ?>>> findArrangementInvolvedParties(org.hibernate.reactive.mutiny.Mutiny.Session session, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements.IArrangement<?, ?> arrangement, String classificationName, String value, com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems<?, ?> system, java.util.UUID... identityToken)
     {
         com.guicedee.activitymaster.fsdm.client.services.IArrangementsService<?> arrangements = com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IArrangementsService.class);
         return arrangements.findArrangementInvolvedParties(session, arrangement, classificationName, value, system, identityToken);
     }

 }



