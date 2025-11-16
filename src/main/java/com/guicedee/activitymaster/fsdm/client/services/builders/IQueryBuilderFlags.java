package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.entityassist.enumerations.ActiveFlag;
import com.entityassist.enumerations.Operand;
import com.entityassist.enumerations.OrderByType;
import com.entityassist.querybuilder.builders.JoinExpression;
import com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.entityassist.enumerations.Operand.*;

@SuppressWarnings("DuplicatedCode")
public interface IQueryBuilderFlags<J
                                        extends IQueryBuilderFlags<J, E, I>,
                                       E extends IWarehouseBaseTable<E, J, I>,
                                       I extends UUID>
    extends IQueryBuilderDefault<J, E, I>
{

  static String listToSqlString(Collection<String> values)
  {
    StringBuilder sb = new StringBuilder();
    for (String value : values)
    {
      sb.append("'")
          .append(value.replace("'", "''"))
          .append("',")
      ;
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  static String listToSqlPostgresCrossTableString(Collection<String> values)
  {
    StringBuilder sb = new StringBuilder();
    for (String value : values)
    {
      sb.append("")
          .append(value.replace("'", "''"))
          .append(" varchar,")
      ;
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  static String buildCaseString(String columnName, String searchName, String valueColumnName)
  {
    return "\t\t\tCASE WHEN " + columnName + " = '" + searchName + "' THEN " + valueColumnName + " END AS \"" + searchName + "\",";
  }

  static String buildAggregrateString(String aggregate, String valueColumnName)
  {
    return "" + aggregate + "(" + valueColumnName + ") AS \"" + valueColumnName + "\",";
  }

  static String listToAggregateSelect(String aggregrate, Collection<String> values)
  {
    StringBuilder sb = new StringBuilder();
    for (String value : values)
    {
      sb.append(buildAggregrateString(aggregrate, value));
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  static String listToCaseSqlPostgresCrossTableString(String columnName, String valueColumnName, Collection<String> values)
  {
    StringBuilder sb = new StringBuilder();
    for (String value : values)
    {
      sb.append(buildCaseString(columnName, value, valueColumnName));
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  static String listToPivotString(Collection<String> values)
  {
    StringBuilder sb = new StringBuilder();
    for (String value : values)
    {
      sb.append("[")
          .append(value.replace("'", "''"))
          .append("],")
      ;
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  @jakarta.validation.constraints.NotNull
  default J inActiveRange()
  {
    List<String> flagRange = new ArrayList<>();
    ActiveFlag.getActiveRangeAndUp()
        .forEach(flag -> flagRange.add(flag.toString()));
    JoinExpression<?, ?, ?> activeFlagJoin = new JoinExpression<>();
    join(getAttribute("activeFlagID"), JoinType.INNER, activeFlagJoin);

    getFilters().add(activeFlagJoin.getFilter("name", Operand.InList, flagRange));
    return (J) this;
  }

  @jakarta.validation.constraints.NotNull
  default J inVisibleRange()
  {
    List<String> flagRange = new ArrayList<>();
    ActiveFlag.getVisibleRangeAndUp()
        .forEach(flag -> flagRange.add(flag.toString()));
    JoinExpression<?, ?, ?> activeFlagJoin = new JoinExpression<>();
    join(getAttribute("activeFlagID"), JoinType.INNER, activeFlagJoin);

    getFilters().add(activeFlagJoin.getFilter("name", Operand.InList, flagRange));
    return (J) this;
  }

  default J latestFirst()
  {
    orderBy(getAttribute("effectiveFromDate"), OrderByType.DESC);
    //noinspection unchecked
    return (J) this;
  }

}
