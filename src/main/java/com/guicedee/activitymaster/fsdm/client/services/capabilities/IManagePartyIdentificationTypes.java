package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyIdentificationType;
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

@SuppressWarnings({"DuplicatedCode", "unused", "rawtypes"})
public interface IManagePartyIdentificationTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
    private String getInvolvedPartyIdentificationTypesRelationshipTable()
    {
        String className = getClass().getCanonicalName() + "XInvolvedPartyIdentificationType";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?>> getInvolvedPartyIdentificationTypeRelationshipClass()
    {
        String joinTableName = getInvolvedPartyIdentificationTypesRelationshipTable();
        try
        {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Cannot find involvedPartyIdentificationType linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures an involved party identification type.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    void configureInvolvedPartyIdentificationTypeAddable(IWarehouseRelationshipTable linkTable, J primary, IInvolvedPartyIdentificationType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, ISystems<?, ?> system);

    /**
     * Finds an involved party identification type with the given classification, identification type, search value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> findInvolvedPartyIdentificationType(Mutiny.Session session, Enum<?> classification, Enum<?> identificationType, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        return findInvolvedPartyIdentificationType(session, classification.toString(), identificationType.toString(), searchValue, system, first, latest, identityToken);
    }

    /**
     * Finds an involved party identification type with the given classification, identification type, search value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> findInvolvedPartyIdentificationType(Mutiny.Session session, String classification, String identificationType, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> relationshipTable = get(getInvolvedPartyIdentificationTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

        return partyService.findInvolvedPartyIdentificationType(session, identificationType, system, identityToken)
                       .chain(involvedPartyIdentificationType -> {
                           IQueryBuilderRelationships<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
                                   = relationshipTable.builder(session)
                                             .findLink((J) this, involvedPartyIdentificationType, null)
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

                           return queryBuilderRelationshipClassification.get()
                                          .map(item -> (IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>) item);
                       });
    }

    /**
     * Finds all involved party identification types with the given classification, identification type, search value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>>> findInvolvedPartyIdentificationTypesAll(Mutiny.Session session, String classification, String identificationType, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> relationshipTable = get(getInvolvedPartyIdentificationTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

        return partyService.findInvolvedPartyIdentificationType(session, identificationType, system, identityToken)
                       .chain(involvedPartyIdentificationType -> {
                           IQueryBuilderRelationships<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
                                   = relationshipTable.builder(session)
                                             .findLink((J) this, involvedPartyIdentificationType, null)
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

                           return queryBuilderRelationshipClassification.getAll()
                                          .map(list -> (List<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>>) list);
                       });
    }

    /**
     * Gets the number of involved party identification types with the given classification value, identification type, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfInvolvedPartyIdentificationTypes(Mutiny.Session session, String classificationValue, String identificationType, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> relationshipTable = get(getInvolvedPartyIdentificationTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

        if (classificationValue == null)
        {
            classificationValue = DefaultClassifications.NoClassification.classificationValue();
        }

        final String finalClassificationValue = classificationValue;

        return partyService.findInvolvedPartyIdentificationType(session, identificationType, system, identityToken)
                       .chain(involvedPartyIdentificationType -> relationshipTable.builder(session)
                                                                         .findLink((J) this, involvedPartyIdentificationType, null)
                                                                         .withValue(value)
                                                                         .withClassification(finalClassificationValue, system)
                                                                         .inActiveRange()
                                                                         .inDateRange()
                                                                         .canRead(system, identityToken)
                                                                         .getCount());
    }

    /**
     * Checks if the entity has involved party identification types with the given classification name, identification type, search value, and system.
     */
    default Uni<Boolean> hasInvolvedPartyIdentificationTypes(Mutiny.Session session, String classificationName, String identificationTypeName, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return numberOfInvolvedPartyIdentificationTypes(session, classificationName, identificationTypeName, searchValue, system, identityToken)
                       .map(count -> count > 0);
    }

    /**
     * Adds an involved party identification type with the given classification name, identification type, value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> addInvolvedPartyIdentificationType(Mutiny.Session session, String classificationName,
                                                                                                                     String involvedPartyIdentificationType,
                                                                                                                     String value,
                                                                                                                     ISystems<?, ?> system,
                                                                                                                     UUID... identityToken)
    {
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyIdentificationType(session, involvedPartyIdentificationType, system, identityToken)
                       .chain(involvedPartyIdentificationType1 -> addInvolvedPartyIdentificationType(session, classificationName, involvedPartyIdentificationType1, value, system, identityToken));
    }

    /**
     * Adds an involved party identification type with the given classification name, identification type, value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> addInvolvedPartyIdentificationType(Mutiny.Session session, String classificationName,
                                                                                                                     IInvolvedPartyIdentificationType<?, ?> involvedPartyIdentificationType,
                                                                                                                     String value,
                                                                                                                     ISystems<?, ?> system,
                                                                                                                     UUID... identityToken)
    {
            IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyIdentificationTypeRelationshipClass());
            IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);

            return classificationService.find(session, classificationName, system, identityToken)
                           .map(classification -> {
                               tableForClassification.setEnterpriseID(system.getEnterpriseID());
                               tableForClassification.setValue(Strings.nullToEmpty(value));
                               tableForClassification.setSystemID(system);
                               tableForClassification.setOriginalSourceSystemID(system.getId());
                               tableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                               tableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
                               tableForClassification.setActiveFlagID(system.getActiveFlagID());
                               tableForClassification.setClassificationID(classification);

                               configureInvolvedPartyIdentificationTypeAddable(tableForClassification, (J) this,
                                       involvedPartyIdentificationType,
                                       classification, value, system);

                               return tableForClassification;
                           })
                           .chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
                           .chain(table -> {
                               // Start the createDefaultSecurity operation but don't wait for it to complete
                               table.createDefaultSecurity(session, system, identityToken);
                               // Return the table immediately without waiting for createDefaultSecurity to complete
                               return Uni.createFrom()
                                              .item((IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>) table);
                           });
    }

    /**
     * Adds or updates an involved party identification type with the given classification value, identification type, search value, store value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> addOrUpdateInvolvedPartyIdentificationType(Mutiny.Session session, String classificationValue,
                                                                                                                             Enum<?> involvedPartyIdentificationType,
                                                                                                                             String searchValue,
                                                                                                                             String storeValue,
                                                                                                                             ISystems<?, ?> system,
                                                                                                                             UUID... identityToken)
    {
        return addOrUpdateInvolvedPartyIdentificationType(session, classificationValue, involvedPartyIdentificationType.toString(), searchValue, storeValue, system, identityToken);
    }

    /**
     * Adds or updates an involved party identification type with the given classification value, identification type, search value, store value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> addOrUpdateInvolvedPartyIdentificationType(Mutiny.Session session, String classificationValue,
                                                                                                                             String involvedPartyIdentificationType,
                                                                                                                             String searchValue,
                                                                                                                             String storeValue,
                                                                                                                             ISystems<?, ?> system,
                                                                                                                             UUID... identityToken)
    {
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyIdentificationType(session, involvedPartyIdentificationType, system, identityToken)
                       .chain(involvedPartyIdentificationType1 -> addOrUpdateInvolvedPartyIdentificationType(session, classificationValue, involvedPartyIdentificationType1, searchValue, storeValue, system, identityToken));
    }

    /**
     * Adds or updates an involved party identification type with the given classification value, identification type, search value, store value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> addOrUpdateInvolvedPartyIdentificationType(Mutiny.Session session, String classificationValue,
                                                                                                                             IInvolvedPartyIdentificationType<?, ?> involvedPartyIdentificationTypeType,
                                                                                                                             String searchValue,
                                                                                                                             String storeValue,
                                                                                                                             ISystems<?, ?> system,
                                                                                                                             UUID... identityToken)
    {
            IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyIdentificationTypeRelationshipClass());
            IClassificationService<?> classificationService = get(IClassificationService.class);

            return classificationService.find(session, classificationValue, system, identityToken)
                           .chain(classification -> {
                               // Create a query to find the existing relationship
                               return tableForClassification.builder(session)
                                              .findLink((J) this, involvedPartyIdentificationTypeType, null)
                                              .withValue(searchValue)
                                              .inActiveRange()
                                              .inDateRange()
                                              .withClassification(classificationValue, system)
                                              //.canCreate(system.getEnterpriseID(), identityToken)
                                              .get()
                                              .onFailure(NoResultException.class)
                                              .recoverWithUni(() -> {
                                                  return (Uni)addInvolvedPartyIdentificationType(session, classificationValue, involvedPartyIdentificationTypeType, storeValue, system, identityToken);
                                              })
                                              .chain(result -> {
                                                  // Cast the result to the correct type
                                                  IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> existingTable =
                                                          (IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?>) result;

                                                  // If the value is the same, return the existing relation
                                                  if (Strings.nullToEmpty(storeValue)
                                                              .equals(existingTable.getValue()))
                                                  {
                                                      return Uni.createFrom()
                                                                     .item((IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>) existingTable);
                                                  }

                                                  // Otherwise, update the relation
                                                  IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                                  return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
                                                                 .chain(archivedFlag -> {
                                                                     existingTable.setActiveFlagID(archivedFlag);
                                                                     existingTable.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                     return session.merge(existingTable);
                                                                 })
                                                                 .chain(() -> {
                                                                     IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getInvolvedPartyIdentificationTypeRelationshipClass());
                                                                     newTableForClassification.setId(null);
                                                                     newTableForClassification.setClassificationID(existingTable.getClassificationID());
                                                                     newTableForClassification.setSystemID(system);
                                                                     newTableForClassification.setOriginalSourceSystemID(existingTable.getId());
                                                                     newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
                                                                     newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                     newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                     newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                     newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));

                                                                     return flagService.getActiveFlag(session, system.getEnterpriseID(), identityToken)
                                                                                    .map(activeFlag -> {
                                                                                        newTableForClassification.setActiveFlagID(activeFlag);
                                                                                        newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                                                        newTableForClassification.setEnterpriseID(system.getEnterpriseID());
                                                                                        configureInvolvedPartyIdentificationTypeAddable(newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(),
                                                                                                classification, storeValue, system);
                                                                                        return newTableForClassification;
                                                                                    });
                                                                 })
                                                                 .chain(newTable -> {
                                                                     return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
                                                                 })
                                                                 .chain(newTable -> {
                                                                     // Start the createDefaultSecurity operation but don't wait for it to complete
                                                                     newTable.createDefaultSecurity(session, system, identityToken);
                                                                     // Return the table immediately without waiting for createDefaultSecurity to complete
                                                                     return Uni.createFrom()
                                                                                    .item((IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>) newTable);
                                                                 });
                                              });
                           });
    }

    /**
     * Adds or reuses an involved party identification type with the given classification value, identification type, search value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> addOrReuseInvolvedPartyIdentificationType(Mutiny.Session session, String classificationValue,
                                                                                                                            String involvedPartyIdentificationTypeType,
                                                                                                                            String searchValue,
                                                                                                                            ISystems<?, ?> system,
                                                                                                                            UUID... identityToken)
    {
        IInvolvedPartyService<?> service = get(IInvolvedPartyService.class);
        return service.findInvolvedPartyIdentificationType(session, involvedPartyIdentificationTypeType, system, identityToken)
                       .chain(involvedPartyIdentificationType -> addOrReuseInvolvedPartyIdentificationType(session, classificationValue, involvedPartyIdentificationType, searchValue, system, identityToken));
    }

    /**
     * Adds or reuses an involved party identification type with the given classification value, identification type, search value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>> addOrReuseInvolvedPartyIdentificationType(Mutiny.Session session, String classificationValue,
                                                                                                                            IInvolvedPartyIdentificationType<?, ?> involvedPartyIdentificationTypeType,
                                                                                                                            String searchValue,
                                                                                                                            ISystems<?, ?> system,
                                                                                                                            UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyIdentificationType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyIdentificationTypeRelationshipClass());

        // Create a query to find the existing relationship
        return tableForClassification.builder(session)
                       .findLink((J) this, involvedPartyIdentificationTypeType, null)
                       .withValue(searchValue)
                       .inActiveRange()
                       .inDateRange()
                       .withClassification(classificationValue, system)
                       //.canCreate(system.getEnterpriseID(), identityToken)
                       .get()
                       .onFailure(NoResultException.class)
                       .recoverWithUni(() -> {
                           return (Uni) addInvolvedPartyIdentificationType(session, classificationValue, involvedPartyIdentificationTypeType, searchValue, system, identityToken);
                       })
                       .map(result -> (IRelationshipValue<J, IInvolvedPartyIdentificationType<?, ?>, ?>) result);
    }
}
