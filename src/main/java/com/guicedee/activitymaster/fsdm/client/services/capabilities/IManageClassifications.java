package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.enumerations.OrderByType;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipClassificationTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import com.guicedee.activitymaster.fsdm.client.services.exceptions.ClassificationException;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.get;

@SuppressWarnings({"DuplicatedCode", "rawtypes", "unchecked"})
public interface IManageClassifications<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
  private String getClassificationsRelationshipTable()
  {
    String className = getClass().getCanonicalName() + "XClassification";
    return className;
  }

  private Class<? extends IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>,
                                                         UUID, ?>> getClassificationsRelationshipClass()
  {
    String joinTableName = getClassificationsRelationshipTable();
    try
    {
      //noinspection unchecked
      return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?>>) Class.forName(joinTableName);
    }
    catch (ClassNotFoundException e)
    {
      throw new RuntimeException("Cannot find classification linked class - " + joinTableName, e);
    }
  }

  default Uni<Boolean> hasClassifications(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return hasClassifications(session, classificationName.toString(), value, system, identityToken);
  }

  default Uni<Boolean> hasClassifications(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return numberOfClassifications(session, classificationName, value, system, identityToken)
               .map(count -> count > 0);
  }

  default Uni<Long> numberOfClassifications(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> relationshipTable.builder(session)
                                            .findLink((J) this, classification, value)
                                            .inActiveRange()
                                            .inDateRange()
                                            .canRead(system, identityToken)
                                            .getCount());
  }

  // Convenience overload: Enum-based classification name
  default Uni<Long> numberOfClassifications(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return numberOfClassifications(session, classificationName.toString(), value, system, identityToken);
  }

  default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, String classificationName, ISystems<?, ?> system, UUID... identityToken)
  {
    IClassificationService<?> classificationService = get(IClassificationService.class);
    IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());

    return classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> {
                 IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilderRelationshipClassification
                     = relationshipTable.builder(session)
                           .findLink((J) this, classification, null)
                           .inActiveRange()
                           .inDateRange()
                           .latestFirst()
                           .withEnterprise(system)
                           .canRead(system, identityToken)
                     ;

                 //noinspection unchecked
                 return queryBuilderRelationshipClassification.getAll()
                            .map(list -> (List<IRelationshipValue<J, IClassification<?, ?>, ?>>) list);
               });
  }

  // Convenience overload: Enum-based classification name
  default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, Enum<?> classificationName, ISystems<?, ?> system, UUID... identityToken)
  {
    return findClassifications(session, classificationName.toString(), system, identityToken);
  }

  default Uni<List<IRelationshipValue<J, IClassification<?, ?>, ?>>> findClassifications(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken)
  {
    IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
    IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilderRelationshipClassification
        = relationshipTable.builder(session)
              .findLink((J) this, null, null)
              .inActiveRange()
              .inDateRange()
              .latestFirst()
              .withEnterprise(system)
              .canRead(system, identityToken)
        ;

    //noinspection unchecked
    return queryBuilderRelationshipClassification.getAll()
               .map(list -> (List<IRelationshipValue<J, IClassification<?, ?>, ?>>) list);
  }

  default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.Session session, Enum<?> classificationName, ISystems<?, ?> system, UUID... identityToken)
  {
    return findClassification(session, classificationName.toString(), system, identityToken);
  }

  default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.Session session, String classificationName, ISystems<?, ?> system, UUID... identityToken)
  {
    return findClassification(session, classificationName, false, system, identityToken);
  }

  default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> findClassification(Mutiny.Session session, String classificationName, boolean latest, ISystems<?, ?> system, UUID... identityToken)
  {
    IWarehouseRelationshipTable<?, ?, J, IClassification<?, ?>, UUID, ?> relationshipTable = get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> {
                 final IClassification<?, ?> finalClassification = classification;
                 IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilderRelationshipClassification
                     = relationshipTable.builder(session)
                           .findLink((J) this, finalClassification, null)
                           .inActiveRange()
                           .inDateRange()
                           .latestFirst()
                           .withEnterprise(system)
                           .canRead(system, identityToken)
                     ;

                 if (latest)
                 {
                   queryBuilderRelationshipClassification.setMaxResults(1)
                       .orderBy(queryBuilderRelationshipClassification.getAttribute("effectiveFromDate"), OrderByType.DESC);
                 }

                 return (Uni<IRelationshipValue<J, IClassification<?, ?>, ?>>) (Uni<?>) queryBuilderRelationshipClassification.get();
               });
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return addClassification(session, classificationName, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, value, system, identityToken);
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addClassification(Mutiny.Session session, String classificationName, EnterpriseClassificationDataConcepts concept, String value, ISystems<?, ?> system, UUID... identityToken)
  {

    IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
        (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return classificationService.find(session, classificationName, concept, system, identityToken)
               .map(classification -> {
                 tableForClassification.setEnterpriseID(system.getEnterpriseID());
                 tableForClassification.setActiveFlagID(system.getActiveFlagID());
                 tableForClassification.setSystemID(system);
                 tableForClassification.setOriginalSourceSystemID(system.getId());
                 tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));

                 tableForClassification.setClassificationID(classification);
                 if (!Strings.isNullOrEmpty(value) && value.length() > 254)
                 {
                   throw new ClassificationException("Message value too long - " + value);
                 }
                 tableForClassification.setValue(value);

                 configureForClassification(session, tableForClassification, classification, system);

                 return tableForClassification;
               })
               .chain(table -> session.persist(table)
                                   .replaceWith(Uni.createFrom()
                                                    .item(table)))
               .chain(table -> {
                 // Execute the createDefaultSecurity operation and wait for it to complete
                 return table.createDefaultSecurity(session, system, identityToken)
                            .map(v -> table); // Return the table after security operation completes
               });
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return addOrUpdateClassification(session, classificationName.toString(), null, value, system, identityToken);
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, Enum<?> classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return addOrUpdateClassification(session, classificationName.toString(), searchValue, value, system, identityToken);
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return addOrUpdateClassification(session, classificationName, null, value, system, identityToken);
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrUpdateClassification(Mutiny.Session session, String classificationName, String searchValue, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
        (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return (Uni) classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> {
                 IQueryBuilderRelationships<?, ?, J, IClassification<?, ?>, UUID> queryBuilder =
                     tableForClassification.builder(session)
                         .findLink((J) this, classification, searchValue)
                         .inActiveRange()
                         .inDateRange()
                         .latestFirst()
                         .canRead(system, identityToken)
                     ;

                 return (Uni<?>)
                            queryBuilder.get()
                                .onFailure(NoResultException.class)
                                .recoverWithUni(
                                    () -> {
                                      return (Uni) addClassification(session, classificationName, value, system, identityToken);
                                    }
                                )
                                .onItem()
                                .call(a -> {
                                  return updateClassification(session, classificationName, value, system, identityToken);
                                });
               });
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrReuseClassification(Mutiny.Session session, Enum<?> classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    return addOrReuseClassification(session, classificationName.toString(), value, system, identityToken);
  }

  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> addOrReuseClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {

    IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
        (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> {
                 return (Uni) tableForClassification.builder(session)
                                  .findLink((J) this, classification, null)
                                  .inActiveRange()
                                  .inDateRange()
                                  .withEnterprise(system.getEnterpriseID())
                                  .canRead(system, identityToken)
                                  .get()
                                  .onFailure(NoResultException.class)
                                  .recoverWithUni(() -> {
                                    return (Uni) addClassification(session, classificationName, value, system, identityToken);
                                  })
                                  .onItem()
                                  .call(a -> {
                                    return Uni.createFrom()
                                               .item((IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) a);
                                  });
               });
  }

  @SuppressWarnings("unchecked")
  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>> updateClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, ?, ?> tableForClassification =
        (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> {
                 final IClassification<?, ?> finalClassification = classification;
                 return tableForClassification.builder(session)
                            .findLink((J) this, finalClassification, null)
                            .inActiveRange()
                            .inDateRange()
                            .latestFirst()
                            .canRead(system, identityToken)
                            .get()
                            .chain(existingTable -> {
                              if (existingTable == null)
                              {
                                return Uni.createFrom()
                                           .failure(new ClassificationException("Unable to find classification"));
                              }
                              else
                              {
                                final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, ?, ?> finalTableForClassification =
                                    (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, ?, ?>) existingTable;
                                if (Strings.nullToEmpty(value)
                                        .equals(existingTable.getValue()))
                                {
                                  return Uni.createFrom()
                                             .item((IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) finalTableForClassification);
                                }

                                final ISystems<?, ?> originalSystem = finalTableForClassification.getSystemID();
                                IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
                                           .chain(archivedFlag -> {
                                             finalTableForClassification.setActiveFlagID(archivedFlag);
                                             finalTableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                             return session.merge(finalTableForClassification);
                                           })
                                           .chain(updatedTable -> {
                                             IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> newTableForClassification =
                                                 (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());

                                             newTableForClassification.setId(null);
                                             newTableForClassification.setClassificationID(finalTableForClassification.getClassificationID());
                                             newTableForClassification.setSystemID(system);
                                             newTableForClassification.setOriginalSourceSystemID(originalSystem.getId());
                                             newTableForClassification.setOriginalSourceSystemUniqueID(finalTableForClassification.getId());
                                             newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                             newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                             newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                             newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));

                                             return flagService.getActiveFlag(session, originalSystem.getEnterpriseID(), identityToken)
                                                        .map(activeFlag -> {
                                                          newTableForClassification.setActiveFlagID(activeFlag);
                                                          newTableForClassification.setValue(value);
                                                          newTableForClassification.setEnterpriseID(system.getEnterpriseID());

                                                          configureForClassification(session, newTableForClassification, finalClassification, system);

                                                          return newTableForClassification;
                                                        });
                                           })
                                           .chain(newTable -> session.persist(newTable)
                                                                  .replaceWith(Uni.createFrom()
                                                                                   .item(newTable)))
                                           .chain(newTable -> {
                                             // Execute the createDefaultSecurity operation and wait for it to complete
                                             return newTable.createDefaultSecurity(session, originalSystem, identityToken)
                                                        .map(v -> (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) newTable); // Return the table after security operation completes
                                           });
                              }
                            });
               });
  }

  @SuppressWarnings("unchecked")
  default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> archiveClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
        (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> {
                 final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> initialTableForClassification = tableForClassification;
                 return tableForClassification.builder(session)
                            .findLink((J) this, classification, null)
                            .inActiveRange()
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(existingTable -> {
                              if (existingTable == null)
                              {
                                return Uni.createFrom()
                                           .item((IRelationshipValue<J, IClassification<?, ?>, ?>) initialTableForClassification);
                              }
                              else
                              {
                                final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> finalTableForClassification =
                                    (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) existingTable;
                                if (Strings.nullToEmpty(value)
                                        .equals(existingTable.getValue()))
                                {
                                  IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                  return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
                                             .chain(archivedFlag -> {
                                               finalTableForClassification.setActiveFlagID(archivedFlag);
                                               finalTableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                               return session.merge(finalTableForClassification);
                                             });
                                }

                                // Value does not match; no-op (return the current link)
                                return Uni.createFrom()
                                           .item((IRelationshipValue<J, IClassification<?, ?>, ?>) existingTable);
                              }
                            });
               });

  }

  @SuppressWarnings("unchecked")
  default Uni<IRelationshipValue<J, IClassification<?, ?>, ?>> removeClassification(Mutiny.Session session, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> tableForClassification =
        (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) get(getClassificationsRelationshipClass());
    IClassificationService<?> classificationService = get(IClassificationService.class);

    return classificationService.find(session, classificationName, system, identityToken)
               .chain(classification -> {
                 final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> initialTableForClassification = tableForClassification;
                 return tableForClassification.builder(session)
                            .findLink((J) this, classification, null)
                            .inActiveRange()
                            .inDateRange()
                            .canRead(system, identityToken)
                            .get()
                            .chain(existingTable -> {
                              if (existingTable == null)
                              {
                                return Uni.createFrom()
                                           .item((IRelationshipValue<J, IClassification<?, ?>, ?>) initialTableForClassification);
                              }
                              else
                              {
                                final IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?> finalTableForClassification =
                                    (IWarehouseRelationshipClassificationTable<?, ?, J, IClassification<?, ?>, UUID, ?>) existingTable;
                                if (Strings.nullToEmpty(value)
                                        .equals(existingTable.getValue()))
                                {
                                  IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                  return flagService.getDeletedFlag(session, system.getEnterpriseID(), identityToken)
                                             .chain(deletedFlag -> {
                                               finalTableForClassification.setActiveFlagID(deletedFlag);
                                               finalTableForClassification.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                               return session.merge(finalTableForClassification);
                                             });
                                }

                                // Value does not match; no-op (return the current link)
                                return Uni.createFrom()
                                           .item((IRelationshipValue<J, IClassification<?, ?>, ?>) existingTable);
                              }
                            });
               });
  }

  void configureForClassification(Mutiny.Session session, IWarehouseRelationshipClassificationTable linkTable, IClassification<?, ?> classificationValue, ISystems<?, ?> system);
}

