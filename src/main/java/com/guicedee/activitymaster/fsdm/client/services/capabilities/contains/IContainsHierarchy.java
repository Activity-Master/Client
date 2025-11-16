package com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;

import com.entityassist.RootEntity;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipClassificationTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
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
    @SuppressWarnings("rawtypes")
    RootEntity me = (RootEntity) this;
    String myTableName = me.getTableName();
    String className = getClass().getCanonicalName() + "X" + (myTableName.indexOf('.') != -1 ? myTableName.substring(myTableName.indexOf('.') + 1) : myTableName);

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

  @SuppressWarnings("DuplicatedCode")
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

}
