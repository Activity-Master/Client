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

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins. Reads verbatim; writes use session.insert +
    // system.getEnterprise() + the stateless resolveDefaultGroupFolderTokens/createDefaultSecurity path.
    // =============================================================================================

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> findAddress(Mutiny.StatelessSession session, String classificationName, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> relationshipTable = get(getAddressRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IAddress<?, ?>, UUID> q
                = relationshipTable.builder(session)
                          .findLink((J) this, null, searchValue)
                          .inActiveRange()
                          .withClassification(classificationName, system)
                          .inDateRange()
                          .withEnterprise(system.getEnterprise())
                          .canRead(system, identityToken);
        if (first) { q.setMaxResults(1); }
        if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
        return q.get()
                       .onItem().ifNull().failWith(() -> new NoSuchElementException("Address not found"))
                       .map(item -> (IRelationshipValue<J, IAddress<?, ?>, ?>) item);
    }

    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IAddress<?, ?>, ?>>> findAddresses(Mutiny.StatelessSession session, String classificationName, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> relationshipTable = get(getAddressRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IAddress<?, ?>, UUID> q
                = relationshipTable.builder(session)
                          .findLink((J) this, null, searchValue)
                          .inActiveRange()
                          .withClassification(classificationName, system)
                          .inDateRange()
                          .withEnterprise(system.getEnterprise())
                          .canRead(system, identityToken);
        if (first) { q.setMaxResults(1); }
        if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
        return q.getAll().map(list -> (List<IRelationshipValue<J, IAddress<?, ?>, ?>>) list);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> addAddress(Mutiny.StatelessSession session, IAddress<?, ?> secondary, String addressClassification, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> tableForClassification = get(getAddressRelationshipClass());
        IClassificationService<?> addressService = get(IClassificationService.class);
        final var enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return addressService.find(session, addressClassification, system, identityToken)
                       .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                               .chain(activeFlag -> {
                                   tableForClassification.setEnterpriseID(enterprise);
                                   tableForClassification.setValue(value);
                                   tableForClassification.setSystemID(system);
                                   tableForClassification.setOriginalSourceSystemID(system.getId());
                                   tableForClassification.setOriginalSourceSystemUniqueID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                                   tableForClassification.setClassificationID(classification);
                                   tableForClassification.setActiveFlagID(activeFlag);
                                   configureAddressLinkValue(tableForClassification, (J) this, secondary, classification, tableForClassification.getValue(), system);
                                   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                           (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                   if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                                   return session.insert(tableForClassification)
                                           .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                   .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                   .onFailure().recoverWithItem(0L))
                                           .replaceWith((IRelationshipValue<J, IAddress<?, ?>, ?>) tableForClassification);
                               }));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> addOrReuseAddress(Mutiny.StatelessSession session, IAddress<?, ?> secondary, String classificationValue, String searchValue, String storeValue, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> tableForClassification = get(getAddressRelationshipClass());
        IClassificationService<?> addressService = get(IClassificationService.class);
        return (Uni) addressService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> tableForClassification.builder(session)
                                       .findLink((J) this, null, null)
                                       .withValue(searchValue)
                                       .inActiveRange()
                                       .inDateRange()
                                       .withClassification(classification)
                                       .get()
                                       .onFailure(NoResultException.class)
                                       .recoverWithUni(() -> (Uni) addAddress(session, secondary, classificationValue, storeValue, system, identityToken))
                                       .chain(result -> Uni.createFrom().item(result)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IAddress<?, ?>, ?>> addOrUpdateAddress(Mutiny.StatelessSession session, IAddress<?, ?> secondary, String classificationValue, String searchValue, String storeValue, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> tableForClassification = get(getAddressRelationshipClass());
        IClassificationService<?> addressService = get(IClassificationService.class);
        final var enterprise = system.getEnterprise();
        return addressService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> tableForClassification.builder(session)
                                       .findLink((J) this, null, null)
                                       .withValue(searchValue)
                                       .inActiveRange()
                                       .inDateRange()
                                       .withClassification(classification)
                                       .get()
                                       .onFailure(NoResultException.class)
                                       .recoverWithUni(() -> (Uni) addAddress(session, secondary, classificationValue, storeValue, system, identityToken))
                                       .chain(result -> {
                                           IRelationshipValue<J, IAddress<?, ?>, ?> existingRelation = (IRelationshipValue<J, IAddress<?, ?>, ?>) result;
                                           if (Strings.nullToEmpty(storeValue).equals(existingRelation.getValue())) {
                                               return Uni.createFrom().item(existingRelation);
                                           }
                                           final IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IAddress<?, ?>, UUID, ?>) result;
                                           IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                           ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                                           return flagService.getArchivedFlag(session, enterprise, identityToken)
                                                   .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
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
                                                       return flagService.getActiveFlag(session, enterprise, identityToken)
                                                               .chain(activeFlag -> {
                                                                   newTableForClassification.setActiveFlagID(activeFlag);
                                                                   newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                                   newTableForClassification.setEnterpriseID(enterprise);
                                                                   configureAddressLinkValue(newTableForClassification, existingTable.getPrimary(), existingTable.getSecondary(), classification, storeValue, system);
                                                                   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                                           (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                                   return session.insert(newTableForClassification)
                                                                           .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                                   .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                                   .onFailure().recoverWithItem(0L))
                                                                           .replaceWith((IRelationshipValue<J, IAddress<?, ?>, ?>) newTableForClassification);
                                                               });
                                                   });
                                       }));
    }
}

