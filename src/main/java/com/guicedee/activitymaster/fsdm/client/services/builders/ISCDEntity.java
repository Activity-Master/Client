package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.entityassist.services.entities.IBaseEntity;

import java.io.Serializable;
import java.time.*;
import java.util.UUID;

public interface ISCDEntity<J extends ISCDEntity<J, Q, I>, Q extends IQueryBuilderSCD<Q, J, I>, I extends UUID>
        extends IBaseEntity<J, Q, I>
{

    /**
     * Returns the effective from date for the given setting
     *
     * @return
     */
    OffsetDateTime getEffectiveFromDate();
    /**
     * Sets the effective from date value for default value
     *
     * @param effectiveFromDate
     *
     * @return
     */
    J setEffectiveFromDate(OffsetDateTime effectiveFromDate);

    /**
     * Returns the effice to date setting for active flag calculation
     *
     * @return
     */
    OffsetDateTime getEffectiveToDate();

    /**
     * Sets the effective to date column value for active flag determination
     *
     * @param effectiveToDate
     * @return This
     */
    J setEffectiveToDate(OffsetDateTime effectiveToDate);

    /**
     * Returns the warehouse created timestamp column value
     *
     * @return The current time
     */
    OffsetDateTime getWarehouseCreatedTimestamp();

    /**
     * Sets the warehouse created timestamp
     *
     * @param warehouseCreatedTimestamp The time to apply
     * @return This
     */
    J setWarehouseCreatedTimestamp(OffsetDateTime warehouseCreatedTimestamp);


    /**
     * Returns the last time the warehouse timestamp column was updated
     *
     * @return The time
     */
    OffsetDateTime getWarehouseLastUpdatedTimestamp();

    /**
     * Sets the last time the warehouse timestamp column was updated
     *
     * @param warehouseLastUpdatedTimestamp
     *
     * @return This
     */
    J setWarehouseLastUpdatedTimestamp(OffsetDateTime warehouseLastUpdatedTimestamp);
}
