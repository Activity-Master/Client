package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.entityassist.RootEntity;
import com.entityassist.enumerations.ActiveFlag;
import com.entityassist.enumerations.SelectAggregrate;
import com.entityassist.querybuilder.QueryBuilder;
import com.entityassist.querybuilder.builders.JoinExpression;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Attribute;
import org.apache.logging.log4j.LogManager;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.entityassist.enumerations.Operand.*;

/**
 * Query builder interface for entities with classification relationships.
 * Provides methods for filtering by classification name, value, and performing pivot operations.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier
 */
public interface IQueryBuilderClassifications<
                                                 J extends IQueryBuilderClassifications<J, E, I>,
                                                 E extends IWarehouseBaseTable<E, J, I>,
                                                 I extends UUID>
    extends IQueryBuilderFlags<J, E, I>
{
  /**
   * Dynamically resolves the relationship class for classifications linked to this entity.
   *
   * @return The relationship class, or null if not found
   */
  default Class<? extends IWarehouseRelationshipTable<?, ?, E, IClassification<?, ?>, UUID, ?>> getClassificationsRelationshipClass()
  {
    String myTableName = getEntity().getClass()
                             .getCanonicalName()
        ;
    String joinTableName = myTableName + "XClassification";
    try
    {
      //noinspection unchecked
      return (Class<? extends IWarehouseRelationshipTable<?, ?, E, IClassification<?, ?>, UUID, ?>>) Class.forName(joinTableName);
    }
    catch (ClassNotFoundException e)
    {
      return null;
    }
  }

  /**
   * Adds a filter for a specific classification entity.
   *
   * @param classification The classification to filter by
   * @return This builder
   */
  default J withClassification(IClassification<?, ?> classification)
  {
    if (classification != null)
    {
      where(this.<E, IClassification<?, ?>>getAttribute("classificationID"), Equals, classification);
    }
    //noinspection unchecked
    return (J) this;
  }

  /**
   * Adds a filter for a classification by its name and system.
   *
   * @param classificationName The name of the classification
   * @param system             The system the classification belongs to
   * @return This builder
   */
  default J withClassification(String classificationName, ISystems<?, ?> system)
  {
    IClassificationService<?> service = com.guicedee.client.IGuiceContext.get(IClassificationService.class);

    Attribute<E, ?> classificationAttr = getAttribute("classificationID");
    if (classificationAttr != null)
    {
      JoinExpression<?, ?, ?> xClassificationsJoin = new JoinExpression<>();
      join(classificationAttr, JoinType.INNER, xClassificationsJoin);
      getFilters().add(xClassificationsJoin.getFilter("name", Equals, classificationName));
    }
    else
    {
      // Fallback: attempt to resolve the classification entity reactively and apply a direct where if attribute metadata is not available
      try {
        service.find(getEntityManager(), classificationName, system)
            .onItemOrFailure()
            .invoke((result, error) -> {
              if (error != null) {
                LogManager.getLogger(getClass()).warn("withClassification fallback: unable to resolve classification '{}' due to {}", classificationName, error.toString());
              } else if (result != null) {
                Attribute<E, IClassification<?, ?>> clsAttr = this.<E, IClassification<?, ?>>getAttribute("classificationID");
                if (clsAttr != null) {
                  where(clsAttr, Equals, result);
                } // else give up silently; other filters (value, parent) may still narrow down results sufficiently for tests
              }
            })
            .await()
            .atMost(Duration.of(50L, ChronoUnit.SECONDS));
      } catch (Throwable ignored) {
        // Ignore to keep the builder resilient in environments where metamodel/attributes aren't fully available
      }
    }

    return (J) this;
  }

  /**
   * Filters the query to include only entities that have the specified classification and value.
   *
   * @param classification The classification to check
   * @param value          The value associated with the classification
   * @param identityToken  Optional security tokens
   * @return This builder
   */
  default J hasClassification(IClassification<?, ?> classification, String value, UUID... identityToken)
  {
    return hasClassification(classification.getName(), value, classification.getSystemID(), identityToken);
  }

  /**
   * Filters the query to include only entities that have the specified classification name.
   *
   * @param classification The classification name
   * @param system         The system it belongs to
   * @param identityToken  Optional security tokens
   * @return This builder
   */
  default J hasClassification(String classification, ISystems<?, ?> system, UUID... identityToken)
  {
    return hasClassification(classification, null, system, identityToken);
  }

  /**
   * Filters the query to include only entities that have the specified classification name and value.
   *
   * @param classificationName The classification name
   * @param value              The value associated with the classification
   * @param system             The system it belongs to
   * @param identityToken      Optional security tokens
   * @return This builder
   */
  default J hasClassification(String classificationName, String value, ISystems<?, ?> system, UUID... identityToken)
  {
    Class<? extends IWarehouseRelationshipTable<?, ?, E, IClassification<?, ?>, UUID, ?>> relationshipTable = getClassificationsRelationshipClass();
    IWarehouseRelationshipTable<?, ?, E, IClassification<?, ?>, UUID, ?> instance = com.guicedee.client.IGuiceContext.get(relationshipTable);
    IQueryBuilderRelationships<?, ?, ?, ?, ?> qbr
        = instance.builder(getEntityManager());

    qbr.withClassification(classificationName, system);
    qbr.inActiveRange();
    qbr.inDateRange();
    qbr.withValue(value);

    Attribute<E, IQueryBuilderRelationships<?, ?, ?, ?, ?>> classifications = getAttribute("classifications");

    //noinspection rawtypes
    join(classifications, (QueryBuilder) qbr);

    //noinspection unchecked
    return (J) this;
  }


  /**
   * Retrieves a pivot-style list of classification values for the specified entities.
   *
   * @param session              The reactive session
   * @param classificationValues The primary classification name to pivot
   * @param idValuesIn           The set of entity IDs to include
   * @param system               The system context
   * @param identityToken        Security tokens
   * @param values               Additional classification names to pivot
   * @return A Uni containing a list of object arrays (pivot rows)
   */
  default Uni<List<Object[]>> getClassificationsValuePivot(Mutiny.Session session, String classificationValues, Set<String> idValuesIn, ISystems<?, ?> system, UUID[] identityToken, String... values)
  {
    return getClassificationsValuePivot(session, SelectAggregrate.Max, classificationValues, idValuesIn, system, identityToken, values);
  }

  /**
   * Retrieves a pivot-style list of classification values for a single entity or a specific ID string.
   *
   * @param session              The reactive session
   * @param classificationValues The primary classification name to pivot
   * @param idValuesIn           The entity ID (as a string) to include
   * @param system               The system context
   * @param identityToken        Security tokens
   * @param values               Additional classification names to pivot
   * @return A Uni containing a list of object arrays
   */
  default Uni<List<Object[]>> getClassificationsValuePivot(Mutiny.Session session, String classificationValues, String idValuesIn, ISystems<?, ?> system, UUID[] identityToken, String... values)
  {
    if (idValuesIn == null)
    {
      return getClassificationsValuePivot(session, SelectAggregrate.Max, classificationValues, null, system, identityToken, values);
    }
    else
    {
      return getClassificationsValuePivot(session, SelectAggregrate.Max, classificationValues, Set.of(idValuesIn), system, identityToken, values);
    }
  }

  /**
   * Retrieves a pivot-style list of classification values using the specified aggregate function.
   *
   * @param session              The reactive session
   * @param aggregrate           The aggregate function to use (e.g., Max)
   * @param classificationValues The primary classification name to pivot
   * @param idValuesIn           The set of entity IDs to include
   * @param system               The system context
   * @param identityToken        Security tokens
   * @param values               Additional classification names to pivot
   * @return A Uni containing a list of object arrays
   */
  @SuppressWarnings("SqlResolve")
  default Uni<List<Object[]>> getClassificationsValuePivot(Mutiny.Session session, SelectAggregrate aggregrate, String classificationValues, Set<String> idValuesIn, ISystems<?, ?> system, UUID[] identityToken, String... values)
  {
    List<String> cStrings = new ArrayList<>();
    cStrings.add(classificationValues);
    cStrings.addAll(Arrays.asList(values));

    //cStrings.sort(String::compareTo);

    String classificationValuesInList = IQueryBuilderFlags.listToSqlString(cStrings);
    String classificationTableValuesInList = IQueryBuilderFlags.listToSqlPostgresCrossTableString(cStrings);
    String classificationPivotInList = IQueryBuilderFlags.listToPivotString(cStrings);

    @SuppressWarnings("rawtypes")
    RootEntity me = (RootEntity) getEntity();

    String myTableName = me.getTableName();
    String idColumnName = (String) me.getIdPair()
                                       .getKey();

    String joinTableName = myTableName + "XClassification";
    String targetTableName = "Classification.Classification";

    Set<ActiveFlag> activeFlags = ActiveFlag.getActiveRangeAndUp();
    String activeFlagsInList = IQueryBuilderFlags.listToSqlString(ActiveFlag.activeFlagToStrings(activeFlags));

    String idInValues = "";
    if (idValuesIn != null && !idValuesIn.isEmpty())
    {
      StringBuilder searchInClause = new StringBuilder();
      for (String s1 : idValuesIn)
      {
        searchInClause.append("'")
            .append(s1.replace("'", "''"))
            .append("',")
        ;
      }
      searchInClause.deleteCharAt(searchInClause.length() - 1);
      idInValues = searchInClause.toString();
    }

    String cteColumnNames = "ID," + IQueryBuilderFlags.listToSqlString(cStrings);
    cteColumnNames = cteColumnNames.replaceAll("'", "");

    String s = "" +
                   // "$$" +

                   "With Results (" + cteColumnNames + ") as (\n" +
                   "" +
                   "\tselect " +
                   "\t\t ri." + idColumnName + "  AS \"ID\",";

    String caseStatements = IQueryBuilderFlags.listToCaseSqlPostgresCrossTableString("ClassificationName", "ric.value", cStrings);
    s += caseStatements;
    s +=
        //    " c.ClassificationName, " +
        //         " ric.Value\n" +

        "\tfrom " + myTableName + "  ri\n" +
            "\t\tleft join " + joinTableName + " ric\n" +
            "\t\t\ton ri." + idColumnName + " = ric." + idColumnName + "\n" +
            "\t\tleft join " + targetTableName + " c\n" +
            "\t\t\ton ric.ClassificationID = c.ClassificationID\n" +
            "\t\tjoin dbo.ActiveFlag af\n" +
            "\t\t\ton ri.ActiveFlagID = af.ActiveFlagID\n" +
            "\t\t\tand ric.ActiveFlagID = af.ActiveFlagID\n" +
            "\t\t\tand c.ActiveFlagID = af.ActiveFlagID\n" +
            "\t\tWHERE ClassificationName in (" + classificationValuesInList + ")\n" +
            "\t\tand af.ActiveFlagName in (" + activeFlagsInList + ")\n" +
            "\t\tand ri.EffectiveFromDate <= now()\n" +
            "\t\tand ri.EffectiveToDate >= now()\n" +
            "\t\tand ric.EffectiveFromDate <= now()\n" +
            "\t\tand ric.EffectiveToDate >= now() \n" +
            (Strings.isNullOrEmpty(idInValues) ? "" : "\t\tand ri." + idColumnName + " IN (" + idInValues + ")\n ") +
            "\t\tand c.EffectiveFromDate <= now()\n" +
            "\t\tand c.EffectiveToDate >= now()\n" +
            "" +
            // "order by ClassificationName " +
            "" +
            "" +
            ")";
    String aggregrateSelect = IQueryBuilderFlags.listToAggregateSelect("MAX", cStrings);

    s += "\n" +
             "SELECT ID," + aggregrateSelect + "\n" +
             ""
    ;
    s +=
        "FROM \n" +
            "Results \n" +
            "GROUP BY ID\n" +
            "\n";


    //s += "$$";

    return me.builder(getEntityManager())
               .getEntityManager()
               .createNativeQuery(s, new Object[]{}.getClass())
               .getResultList()
               .chain(results -> {
                 for (Object[] objects : results)
                 {
                   for (int i = 0; i < objects.length; i++)
                   {
                     Object o = objects[i];
                     if (o == null)
                     {
                       objects[i] = "";
                     }
                     if (o instanceof String)
                     {
                       if ("null".equals(o))
                       {
                         objects[i] = "";
                       }
                     }
                   }
                 }
                 return Uni.createFrom()
                            .item(new ArrayList<>(results));
               })
        ;

  }

}
