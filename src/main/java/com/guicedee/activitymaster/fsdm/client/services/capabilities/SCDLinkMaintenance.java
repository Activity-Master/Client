package com.guicedee.activitymaster.fsdm.client.services.capabilities;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag.IActiveFlag;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.IWarehouseRelationshipTable;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maintenance helpers for Slowly Changing Dimension (SCD) relationship rows.
 *
 * <p>The "update + insert" flows (addOrUpdate / update) need to retire the current active row
 * (set its closing {@link IActiveFlag} and {@code effectiveToDate}) and then insert a brand new
 * version in the same transaction.</p>
 *
 * <p>These stateless flows close the predecessor with a native update and initialize
 * the replacement explicitly before insertion. This avoids enhanced lazy-field update
 * omissions and the current Reactive/ORM module restriction on bulk HQL.</p>
 */
public final class SCDLinkMaintenance
{
    private SCDLinkMaintenance()
    {
    }

    /** A different writer must create a new version even when the stored value is unchanged. */
    public static boolean unchangedBySameSystem(String storedValue, String newValue,
                                                UUID lastWriter, UUID writingSystem) {
        return java.util.Objects.equals(storedValue, newValue == null ? "" : newValue)
                && java.util.Objects.equals(lastWriter, writingSystem);
    }

    /** Initialize a new version before direct stateless insertion, which bypasses builder defaults. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Uni<Void> insertReplacement(Mutiny.StatelessSession session,
                                               IWarehouseRelationshipTable row) {
        row.setId(UUID.randomUUID());
        OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        if (row.getWarehouseCreatedTimestamp() == null) row.setWarehouseCreatedTimestamp(now);
        if (row.getWarehouseLastUpdatedTimestamp() == null) row.setWarehouseLastUpdatedTimestamp(now);
        if (row.getEffectiveFromDate() == null) row.setEffectiveFromDate(now);
        if (row.getEffectiveToDate() == null)
            row.setEffectiveToDate(com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.EndOfTime.atOffset(java.time.ZoneOffset.UTC));
        return session.insert(row);
    }

    /** Close the predecessor directly: stateless updates of enhanced lazy fields can omit
     * effectiveToDate, and bulk HQL is unavailable across the current Reactive/ORM JPMS boundary.
     * SQL identifiers come exclusively from the entity's JPA mapping, never caller input. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Uni<Integer> retireActiveRow(Mutiny.StatelessSession session,
                                               Object managedRow,
                                               UUID rowId,
                                               IActiveFlag<?, ?> closingFlag,
                                               OffsetDateTime effectiveTo)
    {
        IWarehouseRelationshipTable row = (IWarehouseRelationshipTable) managedRow;
        Class<?> entity = managedRow.getClass();
        while (entity != null && !entity.isAnnotationPresent(jakarta.persistence.Table.class))
            entity = entity.getSuperclass();
        if (entity == null) throw new IllegalArgumentException("Relationship has no table mapping");
        jakarta.persistence.Table table = entity.getAnnotation(jakarta.persistence.Table.class);
        String idColumn = null;
        for (Class<?> type = entity; type != null && idColumn == null; type = type.getSuperclass()) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    jakarta.persistence.Column column = field.getAnnotation(jakarta.persistence.Column.class);
                    idColumn = column == null || column.name().isEmpty() ? field.getName() : column.name();
                    break;
                }
            }
        }
        String qualified = identifier(table.schema()) + "." + identifier(table.name());
        return session.createNativeQuery("update " + qualified + " set ActiveFlagID=:flag,"
                        + "EffectiveToDate=:closed,WarehouseLastUpdatedTimestamp=:closed where "
                        + identifier(idColumn) + "=:id and EnterpriseID=:enterprise")
                .setParameter("flag", closingFlag.getId()).setParameter("closed", effectiveTo)
                .setParameter("id", rowId).setParameter("enterprise", row.getEnterpriseID().getId())
                .executeUpdate().invoke(count -> {
                    if (count != 1) throw new IllegalStateException("Relationship predecessor unavailable");
                    row.setActiveFlagID(closingFlag);
                    row.setEffectiveToDate(effectiveTo);
                    row.setWarehouseLastUpdatedTimestamp(effectiveTo);
                });
    }

    private static String identifier(String name) {
        if (name == null || !name.matches("[A-Za-z_][A-Za-z0-9_]*"))
            throw new IllegalArgumentException("Unsupported relationship mapping");
        return name;
    }
}
