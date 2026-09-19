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

    /** String-name stateless variant — resolves the secondary name-type via the stateless party finder, then delegates. */
    default Uni<Void> addOrReuseInvolvedPartyNameType(Mutiny.StatelessSession session, String classificationValue,
                                                      String involvedPartyNameType,
                                                      String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        IInvolvedPartyService<?> service = get(IInvolvedPartyService.class);
        return service.findInvolvedPartyNameType(session, involvedPartyNameType, system, identityToken)
                       .chain(secondary -> addOrReuseInvolvedPartyNameType(session, classificationValue, secondary, searchValue, system, identityToken));
    }

    /** Enum-name stateless variant of {@link #createNameType(Mutiny.StatelessSession, String, String, ISystems, UUID...)}. */
    default Uni<Void> addOrReuseInvolvedPartyNameType(Mutiny.StatelessSession session, String classificationValue,
                                                      IInvolvedPartyNameType<?, ?> secondary,
                                                      String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyNameTypeRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise<?, ?> enterprise = system.getEnterprise();
        return tableForClassification.builder(session)
                       .findLink((J) this, secondary, null)
                       .withValue(searchValue)
                       .inActiveRange()
                       .inDateRange()
                       .withClassification(classificationValue, system)
                       .getCount()
                       .chain(count -> {
                           if (count != null && count > 0)
                           {
                               return Uni.createFrom().voidItem();
                           }
                           return classificationService.find(session, classificationValue, system, identityToken)
                                          .chain(classification -> {
                                              IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
                                              return activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                                                             .chain(activeFlag -> {
                                                                 tableForClassification.setValue(Strings.nullToEmpty(searchValue));
                                                                 tableForClassification.setSystemID(system);
                                                                 tableForClassification.setOriginalSourceSystemID(system.getId());
                                                                 tableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                 tableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
                                                                 tableForClassification.setActiveFlagID(activeFlag);
                                                                 tableForClassification.setClassificationID(classification);
                                                                 tableForClassification.setEnterpriseID(enterprise);
                                                                 configureInvolvedPartyNameTypeAddable(tableForClassification, (J) this, secondary, classification, searchValue, system);
                                                                 com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                                         (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                                                 ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                                                                 if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                                                                 return session.insert(tableForClassification)
                                                                                .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                                                .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                                                .onFailure().recoverWithItem(0L)
                                                                                                .replaceWithVoid());
                                                             });
                                          });
                       });
    }

    // ---- Stateless add (always-insert) name-type link ----

    /** Stateless add involved-party name type (Enum name, value only). */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.StatelessSession session, Enum<?> involvedPartyNameType, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        return addInvolvedPartyNameType(session, involvedPartyNameType.toString(), value, system, identityToken);
    }

    /** Stateless add involved-party name type (String name, value only — NoClassification). */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.StatelessSession session, String involvedPartyNameType, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        return addInvolvedPartyNameType(session, involvedPartyNameType, NoClassification.classificationValue(), value, system, identityToken);
    }

    /** Stateless add involved-party name type (String name with classification) — resolves the secondary via the stateless party finder. */
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.StatelessSession session, String involvedPartyNameType, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyNameType(session, involvedPartyNameType, system, identityToken)
                       .chain(secondary -> addInvolvedPartyNameType(session, secondary, classificationName, value, system, identityToken));
    }

    /** Stateless add involved-party name type (resolved secondary) — always inserts via session.insert + stateless default security. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> addInvolvedPartyNameType(Mutiny.StatelessSession session, IInvolvedPartyNameType<?, ?> involvedPartyNameType, String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyNameTypeRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise<?, ?> enterprise = system.getEnterprise();
        IActiveFlagService<?> activeFlagSvc = get(IActiveFlagService.class);
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        return classificationService.find(session, classificationName, system, identityToken)
                       .chain(classification -> activeFlagSvc.getActiveFlag(session, enterprise, identityToken)
                               .chain(activeFlag -> {
                                   tableForClassification.setValue(Strings.nullToEmpty(value));
                                   tableForClassification.setSystemID(system);
                                   tableForClassification.setOriginalSourceSystemID(system.getId());
                                   tableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                   tableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
                                   tableForClassification.setActiveFlagID(activeFlag);
                                   tableForClassification.setClassificationID(classification);
                                   tableForClassification.setEnterpriseID(enterprise);
                                   configureInvolvedPartyNameTypeAddable(tableForClassification, (J) this, involvedPartyNameType, classification, value, system);
                                   com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                           (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                   if (tableForClassification.getId() == null) { tableForClassification.setId(java.util.UUID.randomUUID()); }
                                   return session.insert(tableForClassification)
                                                  .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                          .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                          .onFailure().recoverWithItem(0L)
                                                          .replaceWithVoid())
                                                  .replaceWith((IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>) tableForClassification);
                               }));
    }

    // ---- Stateless add-or-update (Uni<Void>): SCD retire + re-insert when the stored value changes ----

    /** Enum-name stateless variant of {@link #addOrUpdateInvolvedPartyNameType(Mutiny.StatelessSession, String, String, String, String, ISystems, UUID...)}. */
    default Uni<Void> addOrUpdateInvolvedPartyNameType(Mutiny.StatelessSession session, String classificationValue,
                                                       Enum<?> involvedPartyNameType, String searchValue,
                                                       String storeValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return addOrUpdateInvolvedPartyNameType(session, classificationValue, involvedPartyNameType.toString(), searchValue, storeValue, system, identityToken);
    }

    /** String-name stateless variant — resolves the secondary name-type via the stateless party finder, then delegates. */
    default Uni<Void> addOrUpdateInvolvedPartyNameType(Mutiny.StatelessSession session, String classificationValue,
                                                       String involvedPartyNameType, String searchValue,
                                                       String storeValue, ISystems<?, ?> system, UUID... identityToken)
    {
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyNameType(session, involvedPartyNameType, system, identityToken)
                       .chain(secondary -> addOrUpdateInvolvedPartyNameType(session, classificationValue, secondary, searchValue, storeValue, system, identityToken));
    }

    /**
     * Stateless variant of {@link #addOrUpdateInvolvedPartyNameType(Mutiny.StatelessSession, String, IInvolvedPartyNameType, String, String, ISystems, UUID...)}.
     * A value change retires the active row (full-row {@code session.update}) and inserts a fresh one with the
     * new value + its default security — all on the stateless session.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Uni<Void> addOrUpdateInvolvedPartyNameType(Mutiny.StatelessSession session, String classificationValue,
                                                       IInvolvedPartyNameType<?, ?> secondary,
                                                       String searchValue, String storeValue, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> tableForClassification = get(getInvolvedPartyNameTypeRelationshipClass());
        IClassificationService<?> classificationService = get(IClassificationService.class);
        final com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise<?, ?> enterprise = system.getEnterprise();
        ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
        IActiveFlagService<?> flagService = get(IActiveFlagService.class);

        return classificationService.find(session, classificationValue, system, identityToken)
                       .chain(classification -> tableForClassification.builder(session)
                                      .findLink((J) this, secondary, null)
                                      .withValue(searchValue)
                                      .inActiveRange()
                                      .inDateRange()
                                      .withClassification(classificationValue, system)
                                      .get()
                                      .map(r -> (Object) r)
                                      .onFailure(NoResultException.class)
                                      .recoverWithItem((Object) null)
                                      .chain(existingObj -> {
                                          IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?> existing =
                                                  (IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, java.util.UUID, ?>) existingObj;
                                          if (existing != null && Strings.nullToEmpty(storeValue).equals(existing.getValue()))
                                          {
                                              return Uni.createFrom().voidItem();
                                          }
                                          Uni<Void> retire = (existing == null)
                                                  ? Uni.createFrom().voidItem()
                                                  : flagService.getArchivedFlag(session, enterprise, identityToken)
                                                            .chain(archivedFlag -> {
                                                                existing.setActiveFlagID(archivedFlag);
                                                                existing.setEffectiveToDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                                return session.update(existing).replaceWithVoid();
                                                            });
                                          return retire.chain(() -> flagService.getActiveFlag(session, enterprise, identityToken)
                                                         .chain(activeFlag -> {
                                                             tableForClassification.setId(java.util.UUID.randomUUID());
                                                             tableForClassification.setValue(storeValue == null ? "" : storeValue);
                                                             tableForClassification.setSystemID(system);
                                                             tableForClassification.setOriginalSourceSystemID(system.getId());
                                                             tableForClassification.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                                                             tableForClassification.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
                                                             tableForClassification.setActiveFlagID(activeFlag);
                                                             tableForClassification.setClassificationID(classification);
                                                             tableForClassification.setEnterpriseID(enterprise);
                                                             configureInvolvedPartyNameTypeAddable(tableForClassification, (J) this, secondary, classification, storeValue, system);
                                                             com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable core =
                                                                     (com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable) tableForClassification;
                                                             return session.insert(tableForClassification)
                                                                            .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identityToken)
                                                                                            .chain(tokens -> core.createDefaultSecurity(session, system, enterprise, activeFlag, tokens, identityToken))
                                                                                            .onFailure().recoverWithItem(0L)
                                                                                            .replaceWithVoid());
                                                         }));
                                      }));
    }

    // ---- Stateless relationship-read twins (verbatim; secondary resolved via the stateless party finder) ----

    @SuppressWarnings("unchecked")
    default Uni<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>> findInvolvedPartyNameType(Mutiny.StatelessSession session, String classification, String nameType, String searchValue, ISystems<?, ?> system, boolean first, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyNameTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyNameType(session, nameType, system, identityToken)
                       .chain(involvedPartyNameType -> {
                           IQueryBuilderRelationships<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID> q
                                   = relationshipTable.builder(session)
                                             .findLink((J) this, involvedPartyNameType, null)
                                             .inActiveRange()
                                             .withClassification(classification, system)
                                             .withValue(searchValue)
                                             .inDateRange()
                                             .withEnterprise(system.getEnterprise())
                                             .canRead(system, identityToken);
                           if (first) { q.setMaxResults(1); }
                           if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
                           return q.get().map(item -> (IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>) item);
                       });
    }

    @SuppressWarnings("unchecked")
    default Uni<List<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>>> findInvolvedPartyNameTypesAll(Mutiny.StatelessSession session, String classification, String nameType, String searchValue, ISystems<?, ?> system, boolean latest, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyNameTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        return partyService.findInvolvedPartyNameType(session, nameType, system, identityToken)
                       .chain(involvedPartyNameType -> {
                           IQueryBuilderRelationships<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID> q
                                   = relationshipTable.builder(session)
                                             .findLink((J) this, involvedPartyNameType, null)
                                             .inActiveRange()
                                             .withClassification(classification, system)
                                             .withValue(searchValue)
                                             .inDateRange()
                                             .withEnterprise(system.getEnterprise())
                                             .canRead(system, identityToken);
                           if (latest) { q.orderBy(q.getAttribute("effectiveFromDate")); }
                           return q.getAll().map(list -> (List<IRelationshipValue<J, IInvolvedPartyNameType<?, ?>, ?>>) list);
                       });
    }

    @SuppressWarnings("unchecked")
    default Uni<Long> numberOfInvolvedPartyNameTypes(Mutiny.StatelessSession session, String classificationValue, String nameType, String value, ISystems<?, ?> system, UUID... identityToken)
    {
        IWarehouseRelationshipTable<?, ?, J, IInvolvedPartyNameType<?, ?>, UUID, ?> relationshipTable = get(getInvolvedPartyNameTypeRelationshipClass());
        IInvolvedPartyService<?> partyService = get(IInvolvedPartyService.class);
        final String finalClassificationValue = classificationValue == null ? NoClassification.classificationValue() : classificationValue;
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

    default Uni<Boolean> hasInvolvedPartyNameTypes(Mutiny.StatelessSession session, String classificationName, String nameType, String searchValue, ISystems<?, ?> system, UUID... identityToken)
    {
        return numberOfInvolvedPartyNameTypes(session, classificationName, nameType, searchValue, system, identityToken).map(count -> count > 0);
    }
}
