package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderRelationships;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.convertToUTCDateTime;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime;
import static com.guicedee.client.IGuiceContext.*;

@SuppressWarnings({"DuplicatedCode", "unused"})
public interface IManageResourceItems<J extends IWarehouseBaseTable<J, ?, ? extends Serializable>>
{
    /**
     * Gets the Vertx instance for executing blocking operations.
     * This method should be implemented by classes that implement this interface.
     *
     * @return The Vertx instance
     */
    default Vertx getVertx()
    {
        return get(Vertx.class);
    }

    private String getResourceItemsRelationshipTable()
    {
        String className = getClass().getCanonicalName() + "XResourceItem";
        return className;
    }

    private Class<? extends IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?>> getResourceItemRelationshipClass()
    {
        String joinTableName = getResourceItemsRelationshipTable();
        try
        {
            //noinspection unchecked
            return (Class<? extends IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?>>) Class.forName(joinTableName);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Cannot find resourceItem linked class - " + joinTableName, e);
        }
    }

    /**
     * Configures a resource item link value.
     * <p>
     * This method is non-reactive as it simply sets properties on the linkTable and doesn't perform any actions.
     * It doesn't need to return a Uni as it's a synchronous operation.
     */
    @SuppressWarnings("rawtypes")
    void configureResourceItemAddable(IWarehouseRelationshipTable linkTable, J primary, IResourceItem<?, ?> secondary, IClassification<?, ?> classificationValue, String value, IEnterprise<?, ?> enterprise);

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> findResourceItem(Mutiny.Session session, String classification, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> relationshipTable = get(getResourceItemRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IResourceItem<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
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

        return queryBuilderRelationshipClassification.get()
                       .onItem()
                       .ifNull()
                       .failWith(() -> new NoSuchElementException("Resource item not found"))
                       .map(item -> (IRelationshipValue<J, IResourceItem<?, ?>, ?>) item);
    }

    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IResourceItem<?, ?>, ?>>> findResourceItemsAll(Mutiny.Session session, String classification, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> relationshipTable = get(getResourceItemRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IResourceItem<?, ?>, java.util.UUID> queryBuilderRelationshipClassification
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

        return queryBuilderRelationshipClassification.getAll()
                       .map(list -> (List<IRelationshipValue<J, IResourceItem<?, ?>, ?>>) list);
    }

    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfResourceItems(Mutiny.Session session, String classificationValue, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> relationshipTable = get(getResourceItemRelationshipClass());

        // Prepare classification name
        final String finalClassificationValue = classificationValue == null
                                                        ? DefaultClassifications.NoClassification.classificationValue()
                                                        : classificationValue;

        return relationshipTable.builder(session)
                       .findLink((J) this, null, value)
                       .withClassification(finalClassificationValue, system)
                       .inActiveRange()
                       .inDateRange()
                       .canRead(system, identityToken)
                       .getCount();
    }


    default Uni<Boolean> hasResourceItems(Mutiny.Session session, String resourceItemName, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return numberOfResourceItems(session, resourceItemName, searchValue, system, identityToken)
                       .map(count -> count > 0);
    }

    /**
     * Adds a resource item with the given classification name, resource item, value, and system.
     */
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> addResourceItem(Mutiny.Session session, String classificationName,
                                                                               IResourceItem<?, ?> resourceItem,
                                                                               String value,
                                                                               ISystems<?, ?> system,
                                                                               UUID... identityToken)
    {

        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        String finalClassificationName = classificationName;
        if (Strings.isNullOrEmpty(finalClassificationName))
        {
            finalClassificationName = DefaultClassifications.NoClassification.toString();
        }

        final String finalClassificationNameCopy = finalClassificationName;

        return classificationService.find(session, finalClassificationNameCopy, system, identityToken)
                       .map(classification -> {
                           // Set up the table
                           tableForClassification.setEnterpriseID(system.getEnterpriseID());
                           tableForClassification.setValue(Strings.nullToEmpty(value));
                           tableForClassification.setSystemID(system);
                           tableForClassification.setOriginalSourceSystemID(system.getId());
                           tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                           tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                           tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                           tableForClassification.setActiveFlagID(system.getActiveFlagID());
                           tableForClassification.setClassificationID(classification);

                           //noinspection unchecked
                           configureResourceItemAddable(tableForClassification, (J) this, resourceItem, classification, value, system.getEnterpriseID());

                           return tableForClassification;
                       })
                       .chain(table -> session.persist(table)
                                               .replaceWith(Uni.createFrom()
                                                                    .item(table))
                                               .chain(tableDone -> {
                                                   // Start the createDefaultSecurity operation but don't wait for it to complete
                                                   tableDone.createDefaultSecurity(session, system, identityToken);
                                                   // Return the table immediately without waiting for createDefaultSecurity to complete
                                                   return Uni.createFrom()
                                                                  .item((IRelationshipValue<J, IResourceItem<?, ?>, ?>) tableDone);
                                               }));
    }

    /**
     * Adds or updates a resource item with the given classification value, resource item, search value, store value, and system.
     */
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> addOrUpdateResourceItem(Mutiny.Session session, String classificationValue,
                                                                                       IResourceItem<?, ?> resourceItem,
                                                                                       String searchValue,
                                                                                       String storeValue,
                                                                                       ISystems<?, ?> system,
                                                                                       UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);

        // Prepare classification name
        final String finalClassificationName = Strings.isNullOrEmpty(classificationValue)
                                                       ? DefaultClassifications.NoClassification.toString()
                                                       : classificationValue;

        return classificationService.find(session, finalClassificationName, system, identityToken)
                       .chain(classification -> {
                           // Create a query to find the existing relationship
                           return tableForClassification.builder(session)
                                          .findLink((J) this, null, searchValue)
                                          .inActiveRange()
                                          .withClassification(finalClassificationName, system)
                                          .inDateRange()
                                          .canRead(system, identityToken)
                                          .get()
                                          .onFailure(NoResultException.class)
                                          .recoverWithUni(() -> {
                                              tableForClassification.setEnterpriseID(system.getEnterpriseID());
                                              tableForClassification.setValue(storeValue);
                                              tableForClassification.setSystemID(system);
                                              tableForClassification.setOriginalSourceSystemID(system.getId());
                                              tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                              tableForClassification.setActiveFlagID(system.getActiveFlagID());
                                              tableForClassification.setClassificationID(classification);
                                              configureResourceItemAddable(tableForClassification, (J) this, resourceItem, classification, storeValue, system.getEnterpriseID());

                                              return (Uni) Uni.createFrom()
                                                             .item(tableForClassification)
                                                             .chain(table -> {
                                                                 return session.persist (table).replaceWith(Uni.createFrom().item(table));
                                                             })
                                                             .chain(table -> {
                                                                 table.createDefaultSecurity(session, system, identityToken);
                                                                 return Uni.createFrom()
                                                                                .item((IRelationshipValue<J, IResourceItem<?, ?>, ?>) table);
                                                             });
                                          })
                                          .chain(result -> {

                                              // Cast the result to the correct type
                                              IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> existingTable =
                                                      (IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?>) result;

                                              // If the value is the same, return the existing relation
                                              if (Strings.nullToEmpty(storeValue)
                                                          .equals(existingTable.getValue()))
                                              {
                                                  return Uni.createFrom()
                                                                 .item((IRelationshipValue<J, IResourceItem<?, ?>, ?>) existingTable);
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
                                                                 IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> newTableForClassification = get(getResourceItemRelationshipClass());
                                                                 newTableForClassification.setId(null);
                                                                 newTableForClassification.setClassificationID(existingTable.getClassificationID());
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
                                                                                    newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                                                    newTableForClassification.setEnterpriseID(system.getEnterpriseID());
                                                                                    configureResourceItemAddable(newTableForClassification, (J) existingTable.getPrimary(), resourceItem, classification, storeValue, system.getEnterpriseID());
                                                                                    return newTableForClassification;
                                                                                });
                                                             })
                                                             .chain(newTable -> {
                                                                 return session.persist(newTable)
                                                                                .replaceWith(Uni.createFrom().item(newTable));
                                                             })
                                                             .chain(newTable -> {
                                                                 // Start the createDefaultSecurity operation but don't wait for it to complete
                                                                 newTable.createDefaultSecurity(session, originalSystem, identityToken);
                                                                 // Return the table immediately without waiting for createDefaultSecurity to complete
                                                                 return Uni.createFrom()
                                                                                .item((IRelationshipValue<J, IResourceItem<?, ?>, ?>) newTable);
                                                             });
                                          });
                       });
    }

    /**
     * Adds or reuses a resource item with the given classification value, resource item, search value, and system.
     */
    default Uni<IRelationshipValue<J, IResourceItem<?, ?>, ?>> addOrReuseResourceItem(Mutiny.Session session, String classificationValue,
                                                                                      IResourceItem<?, ?> resourceItem,
                                                                                      String searchValue,
                                                                                      ISystems<?, ?> system,
                                                                                      UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IResourceItem<?, ?>, java.util.UUID, ?> tableForClassification = get(getResourceItemRelationshipClass());

        // Prepare classification name
        final String finalClassificationName = Strings.isNullOrEmpty(classificationValue)
                                                       ? DefaultClassifications.NoClassification.toString()
                                                       : classificationValue;

        // Create a query to find the existing relationship
        return tableForClassification.builder(session)
                       .findLink((J) this, null, searchValue)
                       .inActiveRange()
                       .withClassification(finalClassificationName, system)
                       .inDateRange()
                       .canRead(system, identityToken)
                       .get()
                       .onFailure(NoResultException.class)
                       .recoverWithUni(() -> {
                           return (Uni) addResourceItem(session, finalClassificationName, resourceItem, searchValue, system, identityToken);
                       })
                       .chain(result -> {
                           // Cast the result to the correct type and return it
                           return Uni.createFrom()
                                          .item((IRelationshipValue<J, IResourceItem<?, ?>, ?>) result);
                       });

    }

}

