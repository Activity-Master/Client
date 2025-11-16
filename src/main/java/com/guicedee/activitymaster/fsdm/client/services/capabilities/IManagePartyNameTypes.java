package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyNameType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications.*;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"DuplicatedCode", "unused", "rawtypes"})
public interface IManagePartyNameTypes<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
    private String getInvolvedPartyNameTypesRelationshipTable()
    {
        String className = getClass().getCanonicalName() + "XInvolvedPartyNameType";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?>> getInvolvedPartyNameTypeRelationshipClass()
    {
        String joinTableName = getInvolvedPartyNameTypesRelationshipTable();
        try
        {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?>>) Class.forName(joinTableName);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Cannot find involvedPartyNameType linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures an involved party name type.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    void configureInvolvedPartyNameTypeAddable(IWarehouseRelationshipTable linkTable, J primary, IInvolvedPartyNameType<?, ?> secondary, IClassification<?, ?> classificationValue, String value, ISystems<?, ?> system);

    /**
     * Finds an involved party name type with the given classification, name type, search value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> findInvolvedPartyNameType(Mutiny.Session session, String classification, String nameType, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyNameTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

        return partyService.findInvolvedPartyNameType(session, nameType, system, identityToken)
                       .chain(involvedPartyNameType -> {
                           IQueryBuilderRelationships<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID> queryBuilderRelationshipClassification
                                   = relationshipTable.builder(session)
                                             .findLink((J) this, involvedPartyNameType, null)
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
                                          .map(item -> (IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>) item);
                       });
    }

    /**
     * Finds all involved party name types with the given classification, name type, search value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>>> findInvolvedPartyNameTypesAll(Mutiny.Session session, String classification, String nameType, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyNameTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

        return partyService.findInvolvedPartyNameType(session, nameType, system, identityToken)
                       .chain(involvedPartyNameType -> {
                           IQueryBuilderRelationships<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID> queryBuilderRelationshipClassification
                                   = relationshipTable.builder(session)
                                             .findLink((J) this, involvedPartyNameType, null)
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
                                          .map(list -> (List<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>>) list);
                       });
    }

    /**
     * Gets the number of involved party name types with the given classification value, name type, value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfInvolvedPartyNameTypes(Mutiny.Session session, String classificationValue, String nameType, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyNameTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);

        if (classificationValue == null)
        {
            classificationValue = NoClassification.classificationValue();
        }

        final String finalClassificationValue = classificationValue;

        return partyService.findInvolvedPartyNameType(session, nameType, system, identityToken)
                       .chain(involvedPartyNameType -> relationshipTable.builder(session)
                                                                 .findLink((J) this, involvedPartyNameType, null)
                                                                 .withValue(value)
                                                                 .withClassification(finalClassificationValue, system)
                                                                 .inActiveRange()
                                                                 .inDateRange()
                                                                 .canRead(system, identityToken)
                                                                 .getCount());
    }

    /**
     * Checks if the entity has involved party name types with the given classification name, name type, search value, and system.
     */
    default Uni<Boolean> hasInvolvedPartyNameTypes(Mutiny.Session session, String classificationName, String nameType, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return numberOfInvolvedPartyNameTypes(session, classificationName, nameType, searchValue, system, identityToken)
                       .map(count -> count > 0);
    }

    /**
     * Adds an involved party name type with the given name type, value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.Session session, Enum<?> involvedPartyNameType, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        return addInvolvedPartyNameType(session, involvedPartyNameType.toString(), value, system, identityToken);
    }

    /**
     * Adds an involved party name type with the given name type, value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.Session session, String involvedPartyNameType, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        return addInvolvedPartyNameType(session, involvedPartyNameType, NoClassification.classificationValue(), value, system, identityToken);
    }

    /**
     * Adds an involved party name type with the given name type, classification name, value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.Session session, String involvedPartyNameType, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyNameType(session, involvedPartyNameType, system, identityToken)
                       .chain(involvedPartyNameType1 -> addInvolvedPartyNameType(session, involvedPartyNameType1, classificationName, value, system, identityToken));
    }

    /**
     * Adds an involved party name type with the given name type, classification name, value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.Session session, IInvolvedPartyNameType<?, ?> involvedPartyNameType, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?> tableForClassification = get(getInvolvedPartyNameTypeRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

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

                           configureInvolvedPartyNameTypeAddable(tableForClassification, (J) this,
                                   involvedPartyNameType,
                                   classification, value, system);

                           return tableForClassification;
                       })
                       .chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
                       .chain(table -> {
                           // Start the createDefaultSecurity operation but don't wait for it to complete
                           table.createDefaultSecurity(session, system, identityToken);
                           // Return the table immediately without waiting for createDefaultSecurity to complete
                           return Uni.createFrom()
                                          .item((IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>) table);
                       });
    }

    /**
     * Adds or updates an involved party name type with the given classification value, name type, search value, store value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addOrUpdateInvolvedPartyNameType(Mutiny.Session session, String classificationValue,
                                                                                                         Enum<?> involvedPartyNameType,
                                                                                                         String searchValue,
                                                                                                         String storeValue,
                                                                                                         ISystems<?, ?> system,
                                                                                                         UUID... identityToken)
    {
        return addOrUpdateInvolvedPartyNameType(session, classificationValue, involvedPartyNameType.toString(), searchValue, storeValue, system, identityToken);
    }

    /**
     * Adds or updates an involved party name type with the given classification value, name type, search value, store value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addOrUpdateInvolvedPartyNameType(Mutiny.Session session, String classificationValue,
                                                                                                         String involvedPartyNameType,
                                                                                                         String searchValue,
                                                                                                         String storeValue,
                                                                                                         ISystems<?, ?> system,
                                                                                                         UUID... identityToken)
    {
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyNameType(session, involvedPartyNameType, system, identityToken)
                       .chain(involvedPartyNameType1 -> addOrUpdateInvolvedPartyNameType(session, classificationValue, involvedPartyNameType1, searchValue, storeValue, system, identityToken));
    }

    /**
     * Adds or updates an involved party name type with the given classification value, name type, search value, store value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addOrUpdateInvolvedPartyNameType(Mutiny.Session session, String classificationValue,
                                                                                                         IInvolvedPartyNameType<?, ?> involvedPartyNameTypeType,
                                                                                                         String searchValue,
                                                                                                         String storeValue,
                                                                                                         ISystems<?, ?> system,
                                                                                                         UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyNameTypeRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> {
                           // Create a query to find the existing relationship
                           return tableForClassification.builder(session)
                                          .findLink((J) this, involvedPartyNameTypeType, null)
                                          .withValue(searchValue)
                                          .inActiveRange()
                                          .inDateRange()
                                          .withClassification(classificationValue, system)
                                          //.canCreate(system.getEnterpriseID(), identityToken)
                                          .get()
                                          .onFailure(NoResultException.class)
                                          .recoverWithUni(() -> {
                                              return (Uni) addInvolvedPartyNameType(session, involvedPartyNameTypeType, classificationValue, storeValue, system, identityToken);
                                          })
                                          .chain(result -> {
                                              // Cast the result to the correct type
                                              IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> existingTable =
                                                      (IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?>) result;

                                              // If the value is the same, return the existing relation
                                              if (Strings.nullToEmpty(storeValue)
                                                          .equals(existingTable.getValue()))
                                              {
                                                  return Uni.createFrom()
                                                                 .item((IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>) existingTable);
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
                                                                 IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> newTableForClassification = get(getInvolvedPartyNameTypeRelationshipClass());
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
                                                                                    configureInvolvedPartyNameTypeAddable(newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(),
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
                                                                                .item((IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>) newTable);
                                                             });
                                          });
                       });
    }

    /**
     * Adds or reuses an involved party name type with the given classification value, name type, search value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addOrReuseInvolvedPartyNameType(Mutiny.Session session, String classificationValue,
                                                                                                        String nameTypeString,
                                                                                                        String searchValue,
                                                                                                        ISystems<?, ?> system,
                                                                                                        UUID... identityToken)
    {
        IInvolvedPartyService<?> service = get(IInvolvedPartyService.class);
        return service.findInvolvedPartyNameType(session, nameTypeString, system, identityToken)
                       .chain(nameType -> addOrReuseInvolvedPartyNameType(session, classificationValue, nameType, searchValue, system, identityToken));
    }

    /**
     * Adds or reuses an involved party name type with the given classification value, name type, search value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addOrReuseInvolvedPartyNameType(Mutiny.Session session, String classificationValue,
                                                                                                        IInvolvedPartyNameType<?, ?> involvedPartyNameTypeType,
                                                                                                        String searchValue,
                                                                                                        ISystems<?, ?> system,
                                                                                                        UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyNameTypeRelationshipClass());

        // Create a query to find the existing relationship
        return tableForClassification.builder(session)
                       .findLink((J) this, involvedPartyNameTypeType, null)
                       .withValue(searchValue)
                       .inActiveRange()
                       .inDateRange()
                       .withClassification(classificationValue, system)
                       //.canCreate(system.getEnterpriseID(), identityToken)
                       .get()
                       .onFailure(NoResultException.class)
                       .recoverWithUni(() -> {
                           return (Uni) addInvolvedPartyNameType(session, involvedPartyNameTypeType, classificationValue, searchValue, system, identityToken);
                       })
                       .map(result -> (IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>) result);
    }
}
