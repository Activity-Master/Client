package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.entityassist.services.entities.IBaseEntity;

import java.io.Serializable;
import java.time.*;
import java.util.UUID;

/**
 * Interface for entities that support Slowly Changing Dimension (SCD) behavior.
 * This includes tracking effective dates and warehouse timestamps.
 *
 * @param <J> The entity type
 * @param <Q> The query builder type
 * @param <I> The identifier type
 */
public interface ISCDEntity<J extends ISCDEntity<J, Q, I>, Q extends IQueryBuilderSCD<Q, J, I>, I extends UUID>
        extends IBaseEntity<J, Q, I>
{

    /**
     * Returns the effective from date for this entity.
     *
     * @return The effective from date
     */
    OffsetDateTime getEffectiveFromDate();

    /**
     * Sets the effective from date for this entity.
     *
     * @param effectiveFromDate The date to set
     * @return This entity
     */
    J setEffectiveFromDate(OffsetDateTime effectiveFromDate);

    /**
     * Returns the effective to date for this entity.
     *
     * @return The effective to date
     */
    OffsetDateTime getEffectiveToDate();

    /**
     * Sets the effective to date for this entity.
     *
     * @param effectiveToDate The date to set
     * @return This entity
     */
    J setEffectiveToDate(OffsetDateTime effectiveToDate);

    /**
     * Returns the timestamp when this record was created in the warehouse.
     *
     * @return The creation timestamp
     */
    OffsetDateTime getWarehouseCreatedTimestamp();

    /**
     * Sets the warehouse creation timestamp.
     *
     * @param warehouseCreatedTimestamp The timestamp to set
     * @return This entity
     */
    J setWarehouseCreatedTimestamp(OffsetDateTime warehouseCreatedTimestamp);


    /**
     * Returns the timestamp when this record was last updated in the warehouse.
     *
     * @return The last update timestamp
     */
    OffsetDateTime getWarehouseLastUpdatedTimestamp();

    /**
     * Sets the warehouse last updated timestamp.
     *
     * @param warehouseLastUpdatedTimestamp The timestamp to set
     * @return This entity
     */
    J setWarehouseLastUpdatedTimestamp(OffsetDateTime warehouseLastUpdatedTimestamp);
}
