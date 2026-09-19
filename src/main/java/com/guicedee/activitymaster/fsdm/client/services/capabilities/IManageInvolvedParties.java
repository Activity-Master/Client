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

    // =============================================================================================
    // Stateless (Mutiny.StatelessSession) twins. Reads verbatim; writes use session.insert +
    // system.getEnterprise() + the stateless resolveDefaultGroupFolderTokens/createDefaultSecurity path.
    // =============================================================================================

    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> findInvolvedParty(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IInvolvedParty<?, ?>, UUID> q
                = relationshipTable.builder(session)
                          .findLink((J) this, null, null)
                          .inActiveRange()
                          .withClassification(classification, system)
                          .withValue(searchValue)
                          .inDateRange()
                          .withEnterprise(system.getEnterprise())
                          .canRead(system, identityToken);
        if (first) { q.setMaxResults(1); }
        if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
        return q.get()
                       .onItem().ifNull().failWith(() -> new NoSuchElementException("Involved party not found"))
                       .map(item -> (IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) item);
    }

    default Uni<List<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>>> findInvolvedPartysAll(Mutiny.StatelessSession session, String classification, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyRelationshipClass());
        IQueryBuilderRelationships<?, ?, J, IInvolvedParty<?, ?>, UUID> q
                = relationshipTable.builder(session)
                          .findLink((J) this, null, null)
                          .inActiveRange()
                          .withClassification(classification, system)
                          .withValue(searchValue)
                          .inDateRange()
                          .withEnterprise(system.getEnterprise())
                          .canRead(system, identityToken);
        if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
        return q.getAll().map(list -> (List<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>>) list);
    }

    default Uni<Long> numberOfInvolvedPartys(Mutiny.StatelessSession session, String classificationValue, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyRelationshipClass());
        final String finalClassificationValue = classificationValue == null ? DefaultClassifications.NoClassification.classificationValue() : classificationValue;
        return relationshipTable.builder(session)
                       .findLink((J) this, null, value)
                       .withClassification(finalClassificationValue, system)
                       .inActiveRange()
                       .inDateRange()
                       .canRead(system, identityToken)
                       .getCount();
    }

    default Uni<Boolean> hasInvolvedPartys(Mutiny.StatelessSession session, String involvedPartyTypeName, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return numberOfInvolvedPartys(session, involvedPartyTypeName, searchValue, system, identityToken).map(count -> count > 0);
    }

    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> addInvolvedParty(Mutiny.StatelessSession session, IInvolvedParty<?, ?> involvedParty, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> tableForClassification = get(getInvolvedPartyRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final var enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return classificationService.find(session, classificationName, system, identityToken)
                       .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                               .chain(activeFlag -> {
                                   tableForClassification.setEnterpriseID(enterprise);
                                   tableForClassification.setValue(Strings.nullToEmpty(value));
                                   tableForClassification.setSystemID(system);
                                   tableForClassification.setOriginalSourceSystemID(system.getId());
                                   tableForClassification.setEffectiveFromDate(convertToUTCDateTime(RootEntity.getNow()));
                                   tableForClassification.setEffectiveToDate(EndOfTime.atOffset(ZoneOffset.UTC));
                                   tableForClassification.setActiveFlagID(activeFlag);
                                   tableForClassification.setClassificationID(classification);
                                   configureInvolvedPartyAddable(tableForClassification, (J) this, involvedParty, classification, value, system);
                                   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                           (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                   if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                                   return session.insert(tableForClassification)
                                           .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                   .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                   .onFailure().recoverWithItem(0L))
                                           .replaceWith((IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) tableForClassification);
                               }));
    }

    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> addOrReuseInvolvedParty(Mutiny.StatelessSession session, String classificationValue, IInvolvedParty<?, ?> involvedPartyType, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> tableForClassification = get(getInvolvedPartyRelationshipClass());
        return tableForClassification.builder(session)
                       .findLink((J) this, null, null)
                       .withValue(searchValue)
                       .inActiveRange()
                       .inDateRange()
                       .withClassification(classificationValue, system)
                       .get()
                       .onFailure(NoResultException.class)
                       .recoverWithUni(() -> (Uni) addInvolvedParty(session, involvedPartyType, classificationValue, searchValue, system, identityToken))
                       .chain(result -> Uni.createFrom().item((IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) result));
    }

    default Uni<IRelationshipValue<J, IInvolvedParty<?, ?>, ?>> addOrUpdateInvolvedParty(Mutiny.StatelessSession session, String classificationValue, IInvolvedParty<?, ?> involvedPartyType, String searchValue, String storeValue, ISystems<?, ?> system, UUID... identityToken)
    {
        final IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> tableForClassification = get(getInvolvedPartyRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final var enterprise = system.getEnterprise();
        return classificationService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> tableForClassification.builder(session)
                                       .findLink((J) this, null, null)
                                       .withValue(searchValue)
                                       .inActiveRange()
                                       .inDateRange()
                                       .withClassification(classificationValue, system)
                                       .get()
                                       .onFailure(NoResultException.class)
                                       .recoverWithUni(() -> (Uni) addInvolvedParty(session, involvedPartyType, classificationValue, storeValue, system, identityToken))
                                       .chain(result -> {
                                           IRelationshipValue<J, IInvolvedParty<?, ?>, ?> existingRelation = (IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) result;
                                           if (Strings.nullToEmpty(storeValue).equals(existingRelation.getValue())) {
                                               return Uni.createFrom().item(existingRelation);
                                           }
                                           final IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?> existingTable = (IWarehouseRelationshipTable<?, ?, J, IInvolvedParty<?, ?>, UUID, ?>) result;
                                           IActiveFlagService<?> flagService = get(IActiveFlagService.class);
                                           ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                                           return flagService.getArchivedFlag(session, enterprise, identityToken)
                                                   .chain(archivedFlag -> SCDLinkMaintenance.retireActiveRow(session, existingTable, existingTable.getId(), archivedFlag, convertToUTCDateTime(RootEntity.getNow())))
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
                                                       return flagService.getActiveFlag(session, enterprise, identityToken)
                                                               .chain(activeFlag -> {
                                                                   newTableForClassification.setActiveFlagID(activeFlag);
                                                                   newTableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                                   newTableForClassification.setEnterpriseID(enterprise);
                                                                   configureInvolvedPartyAddable(newTableForClassification, (J) existingTable.getPrimary(), existingTable.getSecondary(), classification, storeValue, system);
                                                                   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                                           (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) newTableForClassification;
                                                                   return session.insert(newTableForClassification)
                                                                           .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                                   .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                                   .onFailure().recoverWithItem(0L))
                                                                           .replaceWith((IRelationshipValue<J, IInvolvedParty<?, ?>, ?>) newTableForClassification);
                                                               });
                                                   });
                                       }));
    }
}
