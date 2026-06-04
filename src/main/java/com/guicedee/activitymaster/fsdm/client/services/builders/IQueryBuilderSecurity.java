package com.guicedee.activitymaster.fsdm.client.services.builders;

import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseSecurityTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseBaseTable;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Logger;


/**
 * Query builder interface for entities with row-level security.
 * Security is enforced when enabled in {@link ActivityMasterConfiguration}.
 *
 * @param <J> The type of the query builder
 * @param <E> The type of the warehouse entity
 * @param <I> The type of the entity identifier
 */
public interface IQueryBuilderSecurity<J extends IQueryBuilderSecurity<J, E, I>,
        E extends IWarehouseBaseTable<E, J, I>,
        I extends UUID>
        extends IQueryBuilderDefault<J, E, I> {

    /**
     * Internal method to determine the security relationship table name.
     *
     * @return The canonical name of the security token entity
     */
    private String getSecuritiessRelationshipTable() {
        String className = getEntity().getClass().getCanonicalName().replace("QueryBuilder", "") + "SecurityToken";
        return className;
    }

    /**
     * Internal method to resolve the security relationship class.
     *
     * @return The security table class
     * @throws RuntimeException if the class cannot be found
     */
    private Class<? extends IWarehouseSecurityTable<?, ?, ?>> getSecuritiesRelationshipClass() {
        String joinTableName = getSecuritiessRelationshipTable();
        try {
            //noinspection unchecked
            return (Class<? extends IWarehouseSecurityTable<?, ?, ?>>) Class.forName(joinTableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find classification linked class - " + joinTableName, e);
        }
    }

    /**
     * Adds a filter to check if the current user/system can read the requested entity.
     * Note: Currently a pass-through when security is disabled in configuration.
     *
     * @param system        The system context
     * @param identityToken Security tokens for the current user/session
     * @return This builder
     */
    @NotNull
    default J canRead(ISystems<?, ?> system, UUID... identityToken) {
        // Security is only enforced when explicitly enabled via configuration.
        // While the flag is off, canRead is a pure pass-through and no row-level
        // security filtering is applied (secure-by-default: the flag defaults to ON).
        if (!ActivityMasterConfiguration.get().isSecurityEnabled()) {
            return (J) this;
        }
        // If no identity tokens provided, skip security filtering (useful for tests and public reads)
        if (identityToken == null || identityToken.length == 0) {
            //todo this must get locked down always expect a populated system calling and the party identity token
            //Logger.getLogger("Security Check").warning("Skipping security check due to no security identity tokens provided.");
            return (J) this;
        }

        Class<? extends IWarehouseSecurityTable<?, ?, ?>> securityRelationshipClass = getSecuritiesRelationshipClass();

        //	J securityJoin = join(getAttribute("securities"));


        return (J) this;
    }

}
