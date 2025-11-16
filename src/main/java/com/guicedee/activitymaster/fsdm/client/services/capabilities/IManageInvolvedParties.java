package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
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

/**
 * Interface for managing involved parties.
 * This interface provides methods for adding, updating, and querying involved parties.
 *
 * @param <J> The type of the entity that implements this interface
 */
@SuppressWarnings({"DuplicatedCode", "rawtypes", "unchecked"})
public interface IManageInvolvedParties<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
    /**
     * Gets the involved parties relationship table name.
     */
    private String getInvolvedPartysRelationshipTable()
    {
        String className = getClass().getCanonicalName() + "XInvolvedParty";
        return className;
    }

    /**
     * Gets the involved party relationship class.
     */
    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?>> getInvolvedPartyRelationshipClass()
    {
        String joinTableName = getInvolvedPartysRelationshipTable();
        try
        {
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?>>) Class.forName(joinTableName);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Cannot find involvedParty linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures an involved party.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    void configureInvolvedPartyAddable(IWarehouseRelationshipTable linkTable, J primary, IInvolvedParty<?, ?> secondary, IClassification<?, ?> classificationValue, String value, ISystems<?, ?> system);

    /**
     * Finds an involved party with the given classification, search value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> findInvolvedParty(Mutiny.Session session, String classification, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IInvolvedParty<?, ?>, UUID> queryBuilderRelationshipClassification
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
                       .failWith(() -> new NoSuchElementException("Involved party not found"))
                       .map(item -> {
                           // Explicit cast to handle type compatibility
                           return (IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) item;
                       });
    }

    /**
     * Finds all involved parties with the given classification, search value, and system.
     */
    default Uni<List<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>>> findInvolvedPartysAll(Mutiny.Session session, String classification, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IInvolvedParty<?, ?>, UUID> queryBuilderRelationshipClassification
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
                           return (List<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>>) list;
                       });
    }

    /**
     * Gets the number of involved parties with the given classification value, value, and system.
     */
    default Uni<Long> numberOfInvolvedPartys(Mutiny.Session session, String classificationValue, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyRelationshipClass());
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

    /**
     * Checks if the entity has involved parties with the given involved party type name, search value, and system.
     */
    default Uni<Boolean> hasInvolvedPartys(Mutiny.Session session, String involvedPartyTypeName, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return numberOfInvolvedPartys(session, involvedPartyTypeName, searchValue, system, identityToken)
                       .map(count -> count > 0);
    }

    /**
     * Adds an involved party with the given involved party, classification name, value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> addInvolvedParty(Mutiny.Session session, IInvolvedParty<?, ?> involvedParty,
                                                                                 String classificationName,
                                                                                 String value,
                                                                                 ISystems<?, ?> system,
                                                                                 UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> tableForClassification = get(getInvolvedPartyRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationName, system, identityToken)
                       .map(classification -> {
                           tableForClassification.setEnterpriseID(system.getEnterpriseID());
                           tableForClassification.setValue(Strings.nullToEmpty(value));
                           tableForClassification.setSystemID(system);
                           tableForClassification.setOriginalSourceSystemID(system.getId());
                           tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                           tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                           tableForClassification.setActiveFlagID(system.getActiveFlagID());
                           tableForClassification.setClassificationID(classification);

                           configureInvolvedPartyAddable(tableForClassification, (J) this,
                                   involvedParty,
                                   classification, value, system);

                           return tableForClassification;
                       })
                       .chain(table -> session.persist(table).replaceWith(Uni.createFrom().item(table)))
                       .chain(table -> {
                           // Chain the security setup operation
                           return table.createDefaultSecurity(session, system, identityToken)
                                   .onFailure().recoverWithNull()  // Continue even if security setup fails
                                   .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) table));
                       });
    }

    /**
     * Adds or updates an involved party with the given classification value, involved party type, search value, store value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> addOrUpdateInvolvedParty(Mutiny.Session session, String classificationValue,
                                                                                         IInvolvedParty<?, ?> involvedPartyType,
                                                                                         String searchValue,
                                                                                         String storeValue,
                                                                                         ISystems<?, ?> system,
                                                                                         UUID... identityToken)
    {
        final IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> tableForClassification = get(getInvolvedPartyRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        return classificationService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> {
                           // Create a query to find the existing relationship
                           IQueryBuilderRelationships<?, ?, J, IInvolvedParty<?, ?>, UUID> query = tableForClassification.builder(session)
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
                                              return (Uni) addInvolvedParty(session, involvedPartyType, classificationValue, storeValue, system, identityToken);
                                          })
                                          .chain(result -> {

                                              // Cast the result to the correct type
                                              IRelationshipValue<J, IInvolvedParty<?, ?>, ?> existingRelation = (IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) result;

                                              // If the value is the same, return the existing relation
                                              if (Strings.nullToEmpty(storeValue)
                                                          .equals(existingRelation.getValue()))
                                              {
                                                  return Uni.createFrom()
                                                                 .item(existingRelation);
                                              }

                                              // Otherwise, update the relation
                                              final IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?>) result;
                                              IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                              return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
                                                             .chain(archivedFlag -> {
                                                                 existingTable.setActiveFlagID(archivedFlag);
                                                                 existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                                                 return session.merge(existingTable);
                                                             })
                                                             .chain(() -> {
                                                                 IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> newTableForClassification = get(getInvolvedPartyRelationshipClass());
                                                                 newTableForClassification.setId(null);
                                                                 newTableForClassification.setClassificationID(existingTable.getClassificationID());
                                                                 newTableForClassification.setSystemID(system);
                                                                 newTableForClassification.setOriginalSourceSystemID(existingTable.getId());
                                                                 newTableForClassification.setOriginalSourceSystemUniqueID(existingTable.getId());
                                                                 newTableForClassification.setWarehouseCreatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                 newTableForClassification.setWarehouseLastUpdatedTimestamp(convertToUTCDateTime(RootEntity.getNow()));
                                                                 newTableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                                                 newTableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));

                                                                 return flagService.getActiveFlag(session, system.getEnterpriseID(), identityToken)
                                                                                .map(activeFlag -> {
                                                                                    newTableForClassification.setActiveFlagID(activeFlag);
                                                                                    newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                                                    newTableForClassification.setEnterpriseID(system.getEnterpriseID());
                                                                                    configureInvolvedPartyAddable(newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(),
                                                                                            classification, storeValue, system);
                                                                                    return newTableForClassification;
                                                                                });
                                                             })
                                                             .chain(newTable -> {
                                                                 return session.persist(newTable).replaceWith(Uni.createFrom().item(newTable));
                                                             })
                                                             .chain(newTable -> {
                                                                 // Chain the security setup operation
                                                                 return newTable.createDefaultSecurity(session, system, identityToken)
                                                                         .onFailure().recoverWithNull()  // Continue even if security setup fails
                                                                         .replaceWith(Uni.createFrom().item((IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) existingTable));
                                                             });
                                          });
                       });
    }

    /**
     * Adds or reuses an involved party with the given classification value, involved party type, search value, and system.
     */
    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> addOrReuseInvolvedParty(Mutiny.Session session, String classificationValue,
                                                                                        IInvolvedParty<?, ?> involvedPartyType,
                                                                                        String searchValue,
                                                                                        ISystems<?, ?> system,
                                                                                        UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> tableForClassification = get(getInvolvedPartyRelationshipClass());

        // Create a query to find the existing relationship
        IQueryBuilderRelationships<?, ?, J, IInvolvedParty<?, ?>, UUID> query = tableForClassification.builder(session)
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
                           return (Uni) addInvolvedParty(session, involvedPartyType, classificationValue, searchValue, system, identityToken);
                       })
                       .chain(result -> {

                           // Cast the result to the correct type and return it
                           return Uni.createFrom()
                                          .item((IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) result);
                       });
    }
}