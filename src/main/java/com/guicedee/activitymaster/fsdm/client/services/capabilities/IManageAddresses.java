package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address.IAddress;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.time.ZoneOffset;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"DuplicatedCode", "UnusedReturnValue", "unused"})
public interface IManageAddresses<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{

    @SuppressWarnings("rawtypes")
    void configureAddressLinkValue(IWarehouseRelationshipTable linkTable, J primary, IAddress<?, ?> secondary, IClassification<?, ?> classificationValue, String value, ISystems<?, ?> system);

    private String getAddressesRelationshipTable()
    {
        String className = getClass().getCanonicalName() + "XAddress";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?>> getAddressRelationshipClass()
    {
        String joinTableName = getAddressesRelationshipTable();
        try
        {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?>>) Class.forName(joinTableName);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Cannot find resourceItem linked class - " + joinTableName, e);
        }
    }

    /**
     * Finds an address with the given classification name, search value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> findAddress(Mutiny.Session session, String classificationName, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> relationshipTable = get(getAddressRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IAddress<?, ?>, UUID> queryBuilderRelationshipClassification
                = relationshipTable.builder(session)
                          .findLink((J) this, null, searchValue)
                          .inActiveRange()
                          .withClassification(classificationName, system)
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
                       .failWith(() -> new NoSuchElementException("Address not found"))
                       .map(item -> {
                           // Explicit cast to handle type compatibility
                           return (IRelationshipValue<J, IAddress<?, ?>, ?>) item;
                       });
    }

    /**
     * Finds all addresses with the given classification name, search value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IAddress<?, ?>, ?>>> findAddresses(Mutiny.Session session, String classificationName, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> relationshipTable = get(getAddressRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IAddress<?, ?>, UUID> queryBuilderRelationshipClassification
                = relationshipTable.builder(session)
                          .findLink((J) this, null, searchValue)
                          .inActiveRange()
                          .withClassification(classificationName, system)
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
        return queryBuilderRelationshipClassification.getAll()
                       .map(list -> {
                           // Explicit cast to handle type compatibility
                           return (List<IRelationshipValue<J, IAddress<?, ?>, ?>>) list;
                       });
    }

    /**
     * Adds an address with the given secondary, address classification, value, and system.
     */
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> addAddress(Mutiny.Session session, IAddress<?, ?> secondary, String addressClassification, String value, ISystems<?, ?> system, UUID... identityToken)
    {

        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> tableForClassification = com.guicedee.client.IGuiceContext.get(getAddressRelationshipClass());
        IClassificationService<?> addressService = get(IClassificationService.class);
        return addressService.find(session, addressClassification, system, identityToken)
                       .map(classification -> {
                           tableForClassification.setEnterpriseID(system.getEnterpriseID());
                           tableForClassification.setValue(value);
                           tableForClassification.setSystemID(system);
                           tableForClassification.setOriginalSourceSystemID(system.getId());
                           tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                           tableForClassification.setClassificationID(classification);
                           tableForClassification.setActiveFlagID(system.getActiveFlagID());
                           //noinspection unchecked
                           configureAddressLinkValue(tableForClassification, (J) this, secondary, classification, tableForClassification.getValue(), system);

                           return tableForClassification;
                       })
                       .chain(table -> session.persist(table)
                                               .replaceWith(Uni.createFrom()
                                                                    .item(table))
                                               .chain(persisted -> {
                                                   // Start the createDefaultSecurity operation but don't wait for it to complete
                                                   persisted.createDefaultSecurity(session, system, identityToken);
                                                   // Return the table immediately without waiting for createDefaultSecurity to complete
                                                   return Uni.createFrom()
                                                                  .item((IRelationshipValue<J, IAddress<?, ?>, ?>) persisted);
                                               }));
    }

    /**
     * Adds or updates an address with the given secondary, classification value, search value, store value, and system.
     */
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> addOrUpdateAddress(Mutiny.Session session, IAddress<?, ?> secondary,
                                                                             String classificationValue,
                                                                             String searchValue,
                                                                             String storeValue,
                                                                             ISystems<?, ?> system,
                                                                             UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> tableForClassification = com.guicedee.client.IGuiceContext.get(getAddressRelationshipClass());
        IClassificationService<?> addressService = get(IClassificationService.class);

        return addressService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> {
                           // Create a query to find the existing relationship
                           IQueryBuilderRelationships<?, ?, J, IAddress<?, ?>, UUID> query = tableForClassification.builder(session)
                                                                                                     .findLink((J) this, null, null)
                                                                                                     .withValue(searchValue)
                                                                                                     .inActiveRange()
                                                                                                     .inDateRange()
                                                                                                     .withClassification(classification)
                                   ;

                           // Get the result and handle it
                           return query.get()
                                          .onFailure(NoResultException.class)
                                          .recoverWithUni(() -> {
                                              return (Uni) addAddress(session, secondary, classificationValue, storeValue, system, identityToken);
                                          })
                                          .chain(result -> {
                                              // Cast the result to the correct type
                                              IRelationshipValue<J, IAddress<?, ?>, ?> existingRelation = (IRelationshipValue<J, IAddress<?, ?>, ?>) result;

                                              // If the value is the same, return the existing relation
                                              if (Strings.nullToEmpty(storeValue)
                                                          .equals(existingRelation.getValue()))
                                              {
                                                  return Uni.createFrom()
                                                                 .item(existingRelation);
                                              }

                                              // Otherwise, update the relation
                                              final IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?>) result;
                                              IActiveFlagService<?> flagService = get(IActiveFlagService.class);

                                              return flagService.getArchivedFlag(session, system.getEnterpriseID(), identityToken)
                                                             .chain(archivedFlag -> {
                                                                 existingTable.setActiveFlagID(archivedFlag);
                                                                 existingTable.setEffectiveToDate(convertToUTCDateTime(RootEntity.getNow()));
                                                                 return session.merge(existingTable);
                                                             })
                                                             .chain(() -> {
                                                                 IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> newTableForClassification = get(getAddressRelationshipClass());
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
                                                                                    configureAddressLinkValue(newTableForClassification, existingTable.getPrimary(), existingTable.getSecondary(),
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
                                                                                .item((IRelationshipValue<J, IAddress<?, ?>, ?>) existingTable);
                                                             });
                                          });
                       });

    }

    /**
     * Adds or reuses an address with the given secondary, classification value, search value, store value, and system.
     */
    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> addOrReuseAddress(Mutiny.Session session, IAddress<?, ?> secondary,
                                                                            String classificationValue,
                                                                            String searchValue,
                                                                            String storeValue,
                                                                            ISystems<?, ?> system,
                                                                            UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> tableForClassification = com.guicedee.client.IGuiceContext.get(getAddressRelationshipClass());
        IClassificationService<?> addressService = get(IClassificationService.class);

        return (Uni) addressService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> {
                           // Create a query to find the existing relationship
                           IQueryBuilderRelationships<?, ?, J, IAddress<?, ?>, UUID> query = tableForClassification.builder(session)
                                                                                                     .findLink((J) this, null, null)
                                                                                                     .withValue(searchValue)
                                                                                                     .inActiveRange()
                                                                                                     .inDateRange()
                                                                                                     .withClassification(classification)
                                   ;

                           // Get the result and handle it
                           return query.get()
                                          .onFailure(NoResultException.class)
                                          .recoverWithUni(() -> {
                                              return (Uni) addAddress(session, secondary, classificationValue, storeValue, system, identityToken);
                                          })
                                          .chain(result -> {
                                              // Cast the result to the correct type and return it
                                              return Uni.createFrom()
                                                             .item((result));
                                          });
                       });
    }
}

