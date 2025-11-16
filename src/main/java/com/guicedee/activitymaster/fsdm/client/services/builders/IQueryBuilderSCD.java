package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.entityassist.enumerations.ActiveFlag;
import com.entityassist.services.querybuilders.IQueryBuilder;
import io.smallrye.mutiny.Uni;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Interface for query builders that work with Slowly Changing Dimension (SCD) entities.
 * This interface includes methods for querying entities within date ranges, handling entity lifecycle,
 * and utility methods for date/time conversions.
 * <p>
 * It also includes methods for working with warehouse tables, such as querying entities in active and visible ranges,
 * deleting entities with a specific active flag type, archiving entities, and updating entities with a new status.
 */
@SuppressWarnings("unused")
public interface IQueryBuilderSCD<J extends IQueryBuilderSCD<J, E, I>,
                                         E extends ISCDEntity<E, J, I>,
                                         I extends UUID>
        extends IQueryBuilder<J, E, I>
{

    /**
     * A timestamp designating the end of time or not applied
     */
    public static final LocalDateTime EndOfTime = LocalDateTime.of(2999, 12, 31, 23, 59, 59, 999);

    /**
     * Where effective from date is greater than today
     *
     * @return This
     */
    J inDateRange();

    /**
     * Returns the effective from and to date to be applied
     * <p>
     * Usually getDate()
     *
     * @param betweenThisDate The date
     * @return This
     */
    J inDateRange(LocalDateTime betweenThisDate);

    /**
     * Returns the effective from and to date to be applied when only the effective date is taken into consideration
     *
     * @param effectiveToDate The date
     * @return This
     */
    J inDateRange(LocalDateTime effectiveToDate, boolean toDate);

    /**
     * In date range from till now
     *
     * @param fromDate The date for from
     * @return This
     */
    J inDateRangeSpecified(LocalDateTime fromDate);

    /**
     * Specifies where effective from date greater and effective to date less than
     *
     * @param fromDate The from date
     * @param toDate   The to date
     * @return This
     */
    J inDateRange(LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Performs any required logic between the original and new entities during an update operation
     * which is a delete and marking of the record as historical, and the insert of a new record which is updated
     * <p>
     * The old and new entities may have the same id, the new entity id is emptied after this call for persistence.
     *
     * @param originalEntity The entity that is going to be deleted
     * @param newEntity      The entity that is going to be created
     * @return currently always true @TODO
     */
    boolean onDeleteUpdate(E originalEntity, E newEntity);

    /**
     * Converts a LocalDateTime to a UTC OffsetDateTime
     *
     * @param ldt The LocalDateTime to convert
     * @return The OffsetDateTime in UTC
     */
    public static OffsetDateTime convertToUTCDateTime(LocalDateTime ldt)
    {
        if (ldt == null)
        {
            return null;
        }
        ZonedDateTime zonedDateTime = ldt.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        OffsetDateTime offsetDateTime = utcZonedDateTime.toOffsetDateTime();
        return offsetDateTime;
    }

    /**
     * Converts an OffsetDateTime to a LocalDateTime in the system default timezone
     *
     * @param ldt The OffsetDateTime to convert
     * @return The LocalDateTime in the system default timezone
     */
    public static LocalDateTime convertToLocalDateTime(OffsetDateTime ldt)
    {
        if (ldt == null)
        {
            return null;
        }
        ZonedDateTime zonedDateTime = ldt.atZoneSameInstant(ZoneId.systemDefault());
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Converts an OffsetDateTime to a LocalDateTime in the specified timezone
     *
     * @param ldt  The OffsetDateTime to convert
     * @param zone The timezone to convert to
     * @return The LocalDateTime in the specified timezone
     */
    public static LocalDateTime convertToLocalDateTime(OffsetDateTime ldt, ZoneId zone)
    {
        if (ldt == null)
        {
            return null;
        }
        ZonedDateTime zonedDateTime = ldt.atZoneSameInstant(zone);
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Converts an OffsetDateTime to a LocalDateTime in the specified timezone
     *
     * @param ldt      The OffsetDateTime to convert
     * @param timezone The timezone ID to convert to
     * @return The LocalDateTime in the specified timezone
     */
    public static LocalDateTime convertToLocalDateTime(OffsetDateTime ldt, String timezone)
    {
        if (ldt == null)
        {
            return null;
        }
        ZonedDateTime zonedDateTime = ldt.atZoneSameInstant(ZoneId.of(timezone));
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Filters from the Active Flag suite where it is in the active range
     *
     * @return This
     */
    J inActiveRange();

    /**
     * Selects all records in the visible range
     *
     * @return This
     */
    J inVisibleRange();

    /**
     * Updates the current record with the given active flag type
     * uses the merge
     *
     * @param newActiveFlagType The new flag type to apply
     * @param entity            The entity to operate on
     * @return The entity
     */
    Uni<E> delete(ActiveFlag newActiveFlagType, E entity);

    /**
     * Marks the record as archived updating the warehouse and effective to date timestamps
     *
     * @param entity The entity
     * @return The Entity as a Uni
     */
    Uni<E> archive(E entity);

    /**
     * Marks the given entity as the given status, with the effective to date and warehouse last updated as now
     * Merges the entity, then detaches,
     * <p>
     * Persists the new record down with the end of time used
     *
     * @param entity The entity
     * @param status The new status
     * @return The updated entity as a Uni
     */
    Uni<E> closeAndReturnNewlyUpdate(E entity, ActiveFlag status);
}
