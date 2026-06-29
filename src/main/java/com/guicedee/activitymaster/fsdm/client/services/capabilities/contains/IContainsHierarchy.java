package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipClassificationTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;
import jakarta.persistence.NoResultException;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications.*;
import static com.guicedee.client.IGuiceContext.*;

public interface IContainsHierarchy<J extends IWarehouseBaseTable<J, ?, I>, I extends java.util.UUID>
{
  private String getHierarchyRelationshipTable()
  {
    // The self-hierarchy join entity is named <EntityClass>X<EntityClass>. Derive the suffix from the
    // entity's simple class name rather than its table name: the table name may be lower-cased (e.g.
    // ResourceItem -> "resourceitem"), which produced an invalid class name (ResourceItemXresourceitem)
    // and a NoClassDefFoundError ("wrong name"). The simple class name preserves the correct casing.
    String className = getClass().getCanonicalName() + "X" + getClass().getSimpleName();

    return className;
  }

  private Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> getHierarchyRelationshipTableClass()
  {
    String joinTableName = getHierarchyRelationshipTable();
    try
    {
      //noinspection unchecked
      return (Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>>) Class.forName(joinTableName);
    }
    catch (ClassNotFoundException e)
    {
      throw new RuntimeException("Cannot find hierarchy linked class - " + joinTableName, e);
    }
  }

  default Uni<J> archiveChild(Mutiny.Session session, IWarehouseTable<?, ?, ? extends Serializable, ?> child, String classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    @SuppressWarnings("unchecked")
    J me = (J) this;
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);

    IClassificationService<?> service = get(IClassificationService.class);
    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    IEnterprise<?, ?> enterprise = system.getEnterprise();
    final String finalClassificationName = classificationName;

    return linkTable.builder(session)
               .findLink(me, (J) child, hierarchyValue)
               .inActiveRange()
               .inDateRange()
               // .canCreate(enterprise, identifyingToken)
               .withClassification(finalClassificationName, system)
               .withEnterprise(enterprise)
               .get()
               .onItem().transformToUni(exists -> {
                 if (exists != null)
                 {
                   IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> table = (IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>) exists;
                   return (Uni<J>) table.archive(session, system, identifyingToken)
                           .replaceWith(me);
                 }
                 return Uni.createFrom().item(me);
               })
               .onFailure()
               .recoverWithItem(me);
  }


  /**
   * Stateless variant of {@link #archiveChild(Mutiny.Session, IWarehouseTable, String, String, ISystems, UUID...)}
   * — finds the active hierarchy link on a {@link Mutiny.StatelessSession} and closes it via the link's stateless
   * {@code archive} (full-row {@code session.update}). No-op when the link is absent.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  default Uni<J> archiveChild(Mutiny.StatelessSession session, IWarehouseTable<?, ?, ? extends Serializable, ?> child, String classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    J me = (J) this;
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);

    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    IEnterprise<?, ?> enterprise = system.getEnterprise();
    final String finalClassificationName = classificationName;

    return linkTable.builder(session)
               .findLink(me, (J) child, hierarchyValue)
               .inActiveRange()
               .inDateRange()
               .withClassification(finalClassificationName, system)
               .withEnterprise(enterprise)
               .get()
               .onItem().transformToUni(exists -> {
                 if (exists != null)
                 {
                   IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> table = (IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>) exists;
                   return (Uni<J>) table.archive(session, system, identifyingToken)
                           .replaceWith(me);
                 }
                 return Uni.createFrom().item(me);
               })
               .onFailure()
               .recoverWithItem(me);
  }


  /**
   * Adds a child with the default hierarchy type classification
   *
   * @param session
   * @param child
   * @param classificationName
   * @param system
   * @param identifyingToken
   * @return
   */
  @SuppressWarnings({"unchecked", "Duplicates"})
  @NotNull
  default Uni<J> addChild(Mutiny.Session session, IWarehouseTable<?, ?, ? extends Serializable, ?> child, String classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    J me = (J) this;
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);

    IClassificationService<?> service = get(IClassificationService.class);
    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    final String finalClassificationName = classificationName;

    return service.find(session, finalClassificationName, system, identifyingToken)
               .chain(classification -> {
                 IEnterprise<?, ?> ent = system.getEnterprise();
                 return linkTable.builder(session)
                            .findLink(me, (J) child, hierarchyValue)
                            .inActiveRange()
                            .inDateRange()
                            // .canCreate(enterprise, identifyingToken)
                            .withClassification(finalClassificationName, system)
                            .withEnterprise(ent)
                            .get()
                            .onItem().transformToUni(exists -> Uni.createFrom().item(me))
                            .onFailure(NoResultException.class).recoverWithUni(() -> {
                              return linkTable.builder(session)
                                      .findLink(me, (J) child, finalClassificationName)
                                      .inActiveRange()
                                      .inDateRange()
                                      .getCount()
                                      .chain(count -> {
                                        if (count > 0)
                                        {
                                          return linkTable.builder(session)
                                                  .findLink(me, (J) child, null)
                                                  .inActiveRange()
                                                  .inDateRange()
                                                  .get()
                                                  .chain(existingLink -> {
                                                    configureNewHierarchyItem((IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>) existingLink, me, (J) child, hierarchyValue);
                                                    return ((IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>) existingLink).update(session, system, identifyingToken)
                                                            .map(updated -> me);
                                                  });
                                        }
                                        else
                                        {
                                          linkTable.setSystemID(system);
                                          linkTable.setActiveFlagID(system.getActiveFlagID());
                                          linkTable.setOriginalSourceSystemID(system.getId());
                                          linkTable.setEnterpriseID(ent);
                                          linkTable.setClassificationID(classification);
                                          linkTable.setValue(Strings.nullToEmpty(hierarchyValue));
                                          configureNewHierarchyItem(linkTable, me, (J) child, hierarchyValue);

                                          return session.persist(linkTable)
                                                  .replaceWith(Uni.createFrom().item(linkTable))
                                                  .chain(persisted -> linkTable.createDefaultSecurity(session, system, identifyingToken)
                                                          .onFailure().invoke(error -> {
                                                            System.err.println("Error in createDefaultSecurity: " + error.getMessage());
                                                          })
                                                          .map(v -> me));
                                        }
                                      });
                            });
               });
  }

  /**
   * Stateless variant of {@link #addChild(Mutiny.Session, IWarehouseTable, String, String, ISystems, UUID...)}.
   * <p>
   * Existence is checked with a scalar {@code getCount()} (never hydrating the {@code @Cacheable} link
   * entity, which a stateless session cannot assemble). The active flag is resolved explicitly (the prepped
   * stateless {@code system} carries a {@code null} eager active-flag association), the link row is written
   * with {@code session.insert(...)}, and its default-security matrix is provisioned through the stateless
   * {@code resolveDefaultGroupFolderTokens} + {@code createDefaultSecurity} path.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @NotNull
  default Uni<J> addChild(Mutiny.StatelessSession session, IWarehouseTable<?, ?, ? extends Serializable, ?> child, String classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    J me = (J) this;
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);

    IClassificationService<?> service = get(IClassificationService.class);
    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    final String finalClassificationName = classificationName;
    IEnterprise<?, ?> ent = system.getEnterprise();

    return service.find(session, finalClassificationName, system, identifyingToken)
               .chain(classification -> linkTable.builder(session)
                          .findLink(me, (J) child, hierarchyValue)
                          .inActiveRange()
                          .inDateRange()
                          .withClassification(finalClassificationName, system)
                          .withEnterprise(ent)
                          .getCount()
                          .chain(existing -> {
                            if (existing != null && existing > 0)
                            {
                              return Uni.createFrom().item(me);
                            }
                            IActiveFlagService<?> afs = get(IActiveFlagService.class);
                            ISecurityTokenService<?> sts = get(ISecurityTokenService.class);
                            return afs.getActiveFlag(session, ent, identifyingToken)
                                       .chain(af -> {
                                         linkTable.setSystemID(system);
                                         linkTable.setActiveFlagID((IActiveFlag<?, ?>) af);
                                         linkTable.setOriginalSourceSystemID(system.getId());
                                         linkTable.setEnterpriseID(ent);
                                         linkTable.setClassificationID(classification);
                                         linkTable.setValue(Strings.nullToEmpty(hierarchyValue));
                                         configureNewHierarchyItem(linkTable, me, (J) child, hierarchyValue);
                                         return session.insert(linkTable)
                                                    .chain(() -> sts.resolveDefaultGroupFolderTokens(session, system, identifyingToken)
                                                               .chain(tokens -> ((IWarehouseCoreTable) linkTable)
                                                                          .createDefaultSecurity(session, system, ent, af, tokens))
                                                               .onFailure().recoverWithItem(0L))
                                                    .replaceWith(me);
                                       });
                          }));
  }

  /**
   * Finds the direct parent on A Hierarchy Type
   *
   * @param session
   * @param identifyingToken
   * @return
   */
  @SuppressWarnings("unchecked")
  default Uni<J> findParent(Mutiny.Session session, String hierarchyValue, String classificationName, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    if (Strings.isNullOrEmpty(hierarchyValue))
    {
      hierarchyValue = null;
    }
    final String finalHierarchyValue = hierarchyValue;
    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    final String finalClassificationName = classificationName;

    return (Uni) linkTable.builder(session)
                     .findLink(null, (J) this, null)
                     .inActiveRange()
                     .withClassification(finalClassificationName, system)
                     .inDateRange()
                     .withValue(finalHierarchyValue)
                     .canRead(system, identifyingToken)
                     .withEnterprise(system.getEnterprise())
                     .get()
                     .onItem().transform(exists -> {
                       IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> q = (IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>) exists;
                       return q.getPrimary();
                     })
                     .onFailure(NoResultException.class).recoverWithItem(() -> null);
  }

  /**
   * Finds the direct parent on A Hierarchy Type
   *
   * @param session
   * @param identifyingToken
   * @return
   */
  @SuppressWarnings("unchecked")
  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> findParentLink(Mutiny.Session session, String hierarchyValue, String classificationName, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    if (Strings.isNullOrEmpty(hierarchyValue))
    {
      hierarchyValue = null;
    }
    final String finalHierarchyValue = hierarchyValue;
    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    final String finalClassificationName = classificationName;

    return (Uni) linkTable.builder(session)
                     .findLink(null, (J) this, null)
                     .inActiveRange()
                     .withClassification(finalClassificationName, system)
                     .inDateRange()
                     .withValue(finalHierarchyValue)
                     .canRead(system, identifyingToken)
                     .withEnterprise(system.getEnterprise())
                     .get()
                     .onFailure(NoResultException.class).recoverWithItem(() -> null);
  }

  /**
   * Finds the direct parent on A Hierarchy Type
   *
   * @param session
   * @param identifyingToken
   * @return
   */
  @SuppressWarnings("unchecked")
  default Uni<List<IRelationshipValue<J, J, ?>>> findParents(Mutiny.Session session, String hierarchyValue, String classificationName, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    if (Strings.isNullOrEmpty(hierarchyValue))
    {
      hierarchyValue = null;
    }
    final String finalHierarchyValue = hierarchyValue;
    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    final String finalClassificationName = classificationName;

    return linkTable.builder(session)
               .findLink(null, (J) this, finalHierarchyValue)
               .inActiveRange()
               .withClassification(finalClassificationName, system)
               .inDateRange()
               .canRead(system, identifyingToken)
               .withEnterprise(system.getEnterprise())
               .getAll()
               .map(list -> (List<IRelationshipValue<J, J, ?>>) list);
  }

  default Uni<List<IRelationshipValue<J, J, ?>>> findChildren(Mutiny.Session session, Enum<?> classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    return findChildren(session, classificationName.toString(), hierarchyValue, system, identifyingToken);
  }

  @SuppressWarnings("unchecked")
  default Uni<List<IRelationshipValue<J, J, ?>>> findChildren(Mutiny.Session session, String classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    if (Strings.isNullOrEmpty(hierarchyValue))
    {
      hierarchyValue = null;
    }
    final String finalHierarchyValue = hierarchyValue;
    if (Strings.isNullOrEmpty(classificationName))
    {
      classificationName = HierarchyTypeClassification.toString();
    }
    final String finalClassificationName = classificationName;

    return linkTable.builder(session)
               .findLink((J) this, null, finalHierarchyValue)
               .inActiveRange()
               .withClassification(finalClassificationName, system)
               .inDateRange()
               .canRead(system, identifyingToken)
               .withEnterprise(system.getEnterprise())
               .getAll()
               .map(list -> (List<IRelationshipValue<J, J, ?>>) list);
  }

  void configureNewHierarchyItem(IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> newLink, J parent, J child, String value);

  // =============================================================================================
  // Stateless (Mutiny.StatelessSession) read twins. The hierarchy query builder is session-polymorphic
  // and the bodies use system.getEnterprise() (no session.fetch), so these are verbatim session swaps.
  // addChild already has a stateless overload above; archiveChild (a close-mutation via table.archive)
  // is intentionally not twinned here.
  // =============================================================================================

  @SuppressWarnings("unchecked")
  default Uni<J> findParent(Mutiny.StatelessSession session, String hierarchyValue, String classificationName, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    final String finalHierarchyValue = Strings.isNullOrEmpty(hierarchyValue) ? null : hierarchyValue;
    final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? HierarchyTypeClassification.toString() : classificationName;
    return (Uni) linkTable.builder(session)
                     .findLink(null, (J) this, null)
                     .inActiveRange()
                     .withClassification(finalClassificationName, system)
                     .inDateRange()
                     .withValue(finalHierarchyValue)
                     .canRead(system, identifyingToken)
                     .withEnterprise(system.getEnterprise())
                     .get()
                     .onItem().transform(exists -> {
                       IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> q = (IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>) exists;
                       return q.getPrimary();
                     })
                     .onFailure(NoResultException.class).recoverWithItem(() -> null);
  }

  @SuppressWarnings("unchecked")
  default Uni<IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> findParentLink(Mutiny.StatelessSession session, String hierarchyValue, String classificationName, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    final String finalHierarchyValue = Strings.isNullOrEmpty(hierarchyValue) ? null : hierarchyValue;
    final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? HierarchyTypeClassification.toString() : classificationName;
    return (Uni) linkTable.builder(session)
                     .findLink(null, (J) this, null)
                     .inActiveRange()
                     .withClassification(finalClassificationName, system)
                     .inDateRange()
                     .withValue(finalHierarchyValue)
                     .canRead(system, identifyingToken)
                     .withEnterprise(system.getEnterprise())
                     .get()
                     .onFailure(NoResultException.class).recoverWithItem(() -> null);
  }

  @SuppressWarnings("unchecked")
  default Uni<List<IRelationshipValue<J, J, ?>>> findParents(Mutiny.StatelessSession session, String hierarchyValue, String classificationName, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    final String finalHierarchyValue = Strings.isNullOrEmpty(hierarchyValue) ? null : hierarchyValue;
    final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? HierarchyTypeClassification.toString() : classificationName;
    return linkTable.builder(session)
               .findLink(null, (J) this, finalHierarchyValue)
               .inActiveRange()
               .withClassification(finalClassificationName, system)
               .inDateRange()
               .canRead(system, identifyingToken)
               .withEnterprise(system.getEnterprise())
               .getAll()
               .map(list -> (List<IRelationshipValue<J, J, ?>>) list);
  }

  default Uni<List<IRelationshipValue<J, J, ?>>> findChildren(Mutiny.StatelessSession session, Enum<?> classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    return findChildren(session, classificationName.toString(), hierarchyValue, system, identifyingToken);
  }

  @SuppressWarnings("unchecked")
  default Uni<List<IRelationshipValue<J, J, ?>>> findChildren(Mutiny.StatelessSession session, String classificationName, String hierarchyValue, ISystems<?, ?> system, UUID... identifyingToken)
  {
    Class<? extends IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?>> hierarchyTable = getHierarchyRelationshipTableClass();
    IWarehouseRelationshipClassificationTable<?, ?, J, J, I, ?> linkTable = get(hierarchyTable);
    final String finalHierarchyValue = Strings.isNullOrEmpty(hierarchyValue) ? null : hierarchyValue;
    final String finalClassificationName = Strings.isNullOrEmpty(classificationName) ? HierarchyTypeClassification.toString() : classificationName;
    return linkTable.builder(session)
               .findLink((J) this, null, finalHierarchyValue)
               .inActiveRange()
               .withClassification(finalClassificationName, system)
               .inDateRange()
               .canRead(system, identifyingToken)
               .withEnterprise(system.getEnterprise())
               .getAll()
               .map(list -> (List<IRelationshipValue<J, J, ?>>) list);
  }

}
