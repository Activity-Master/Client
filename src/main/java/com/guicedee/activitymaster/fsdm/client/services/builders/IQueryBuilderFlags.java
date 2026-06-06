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

/**
 * Query builder interface for entities with active flags.
 * Provides methods for filtering based on active and visible ranges.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier
 */
public interface IQueryBuilderFlags<J
                                        extends IQueryBuilderFlags<J, E, I>,
                                       E extends IWarehouseBaseTable<E, J, I>,
                                       I extends UUID>
    extends IQueryBuilderDefault<J, E, I>
{

  /**
   * Converts a collection of strings to a SQL-safe comma-separated string for IN clauses.
   *
   * @param values The values to convert
   * @return The formatted SQL string
   */
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

  /**
   * Converts a collection of strings to a SQL-safe string for PostgreSQL cross-table definitions.
   *
   * @param values The values to convert
   * @return The formatted SQL string
   */
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

  /**
   * Builds a CASE WHEN string for SQL pivot operations.
   *
   * @param columnName      The name of the column to check
   * @param searchName      The value to search for
   * @param valueColumnName The name of the value column to return
   * @return The CASE statement string
   */
  static String buildCaseString(String columnName, String searchName, String valueColumnName)
  {
    return "\t\t\tCASE WHEN " + columnName + " = '" + searchName + "' THEN " + valueColumnName + " END AS \"" + searchName + "\",";
  }

  /**
   * Builds an aggregate function string for SQL queries.
   *
   * @param aggregate       The aggregate function (e.g., MAX, SUM)
   * @param valueColumnName The column to aggregate
   * @return The aggregate expression string
   */
  static String buildAggregrateString(String aggregate, String valueColumnName)
  {
    return "" + aggregate + "(" + valueColumnName + ") AS \"" + valueColumnName + "\",";
  }

  /**
   * Converts a collection of values to an aggregate select string.
   *
   * @param aggregrate The aggregate function
   * @param values     The column values to aggregate
   * @return The formatted SELECT expressions
   */
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

  /**
   * Converts a collection of values to a CASE statement select string for PostgreSQL.
   *
   * @param columnName      The column name
   * @param valueColumnName The value column name
   * @param values          The values to generate cases for
   * @return The formatted CASE expressions
   */
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

  /**
   * Converts a collection of strings to a SQL-safe pivot column string.
   *
   * @param values The values to convert
   * @return The formatted pivot string
   */
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

  /**
   * Filters the query to only include records within the active range.
   *
   * @return This builder
   */
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

  /**
   * Filters the query to only include records within the visible range.
   *
   * @return This builder
   */
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

  /**
   * Orders the results by effective from date descending (latest first).
   *
   * @return This builder
   */
  default J latestFirst()
  {
    orderBy(getAttribute("effectiveFromDate"), OrderByType.DESC);
    //noinspection unchecked
    return (J) this;
  }

}
