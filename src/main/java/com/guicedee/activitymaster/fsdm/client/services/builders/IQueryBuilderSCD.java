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
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier
 */
@SuppressWarnings("unused")
public interface IQueryBuilderSCD<J extends IQueryBuilderSCD<J, E, I>,
                                         E extends ISCDEntity<E, J, I>,
                                         I extends UUID>
        extends IQueryBuilder<J, E, I>
{

    /**
     * A timestamp designating the end of time (2999-12-31), used for active records.
     */
    public static final LocalDateTime EndOfTime = LocalDateTime.of(2999, 12, 31, 23, 59, 59, 999);

    /**
     * Filters for entities where the effective from date is greater than today.
     *
     * @return This builder
     */
    J inDateRange();

    /**
     * Filters for entities that are effective on the specified date.
     *
     * @param betweenThisDate The date to check effectiveness for
     * @return This builder
     */
    J inDateRange(LocalDateTime betweenThisDate);

    /**
     * Filters for entities based on the effective to date.
     *
     * @param effectiveToDate The to date to check
     * @param toDate          Whether to filter specifically on the to date
     * @return This builder
     */
    J inDateRange(LocalDateTime effectiveToDate, boolean toDate);

    /**
     * Filters for entities effective from a specified date until now.
     *
     * @param fromDate The start date
     * @return This builder
     */
    J inDateRangeSpecified(LocalDateTime fromDate);

    /**
     * Filters for entities effective within the specified start and end dates.
     *
     * @param fromDate The start date
     * @param toDate   The end date
     * @return This builder
     */
    J inDateRange(LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Performs any required logic between the original and new entities during an update operation.
     * This typically involves marking the original record as historical and inserting a new updated record.
     *
     * @param originalEntity The entity that is being replaced/deleted
     * @param newEntity      The new entity that will replace it
     * @return true if successful
     */
    boolean onDeleteUpdate(E originalEntity, E newEntity);

    /**
     * Converts a LocalDateTime to a UTC OffsetDateTime.
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
     * Converts an OffsetDateTime to a LocalDateTime in the system default timezone.
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
     * Converts an OffsetDateTime to a LocalDateTime in the specified timezone.
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
     * Converts an OffsetDateTime to a LocalDateTime in the specified timezone ID.
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
     * Filters records that are within the active range based on their active flag.
     *
     * @return This builder
     */
    J inActiveRange();

    /**
     * Filters records that are within the visible range based on their active flag.
     *
     * @return This builder
     */
    J inVisibleRange();

    /**
     * "Deletes" an entity by updating its active flag to the specified type.
     *
     * @param newActiveFlagType The new active flag status (e.g., Deleted)
     * @param entity            The entity to update
     * @return A Uni containing the updated entity
     */
    Uni<E> delete(ActiveFlag newActiveFlagType, E entity);

    /**
     * Marks the record as archived by updating warehouse and effective to date timestamps.
     *
     * @param entity The entity to archive
     * @return A Uni containing the archived entity
     */
    Uni<E> archive(E entity);

    /**
     * Closes the current record and returns a newly created record with the specified status.
     * Used for SCD type 2 updates.
     *
     * @param entity The current entity
     * @param status The status for the new record
     * @return A Uni containing the newly created entity
     */
    Uni<E> closeAndReturnNewlyUpdate(E entity, ActiveFlag status);
}
