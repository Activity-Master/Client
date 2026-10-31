package com.guicedee.activitymaster.fsdm.client.services;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import java.time.OffsetDateTime;
import java.util.*;

/** Canonical applicable-token expansion, shared by standalone and composed reads.
 * Prepare after any authority lock wait; never retain this request state across transactions. */
public record ApplicableTokenQuery(UUID enterprise, OffsetDateTime now, Collection<UUID> visible, List<String> identities) {
    public ApplicableTokenQuery {
        visible = List.copyOf(visible); identities = List.copyOf(identities);
        if(identities.isEmpty()) throw new IllegalArgumentException("An identifying credential is required");
    }

    public static Uni<ApplicableTokenQuery> prepare(Mutiny.StatelessSession session, ISystems<?,?> system, UUID... identities) {
        List<String> values = Arrays.stream(Objects.requireNonNull(identities)).filter(Objects::nonNull).map(UUID::toString).toList();
        if(values.isEmpty()) return Uni.createFrom().failure(new IllegalArgumentException("An identifying credential is required"));
        OffsetDateTime now = com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD
                .convertToUTCDateTime(com.entityassist.RootEntity.getNow());
        IActiveFlagService<?> flags = com.guicedee.client.IGuiceContext.get(IActiveFlagService.class);
        return flags.getVisibleRangeAndUpIds(session, system.getEnterprise())
                .map(visible -> new ApplicableTokenQuery(system.getEnterprise().getId(),now,visible,values));
    }

    public <T> Mutiny.SelectionQuery<T> bind(Mutiny.SelectionQuery<T> query) {
        return query.setParameter("applicableIdentities",identities).setParameter("applicableEnterprise",enterprise)
                .setParameter("applicableNow",now).setParameter("applicableVisible",visible);
    }

    public String cte() {
        return "with recursive applicable(securitytokenid) as ( " +
			                             "    select st.securitytokenid " +
			                             "    from security.securitytoken st " +
			                             "    join classification.classification identitytype on identitytype.classificationid=st.securitytokenclassificationid " +
			                             "    where st.securitytoken in (:applicableIdentities) " +
			                             "      and st.enterpriseid = :applicableEnterprise " +
			                             "      and identitytype.enterpriseid = :applicableEnterprise and identitytype.classificationname <> 'Plugin' " +
			                             "      and st.effectivefromdate <= :applicableNow and st.effectivetodate > :applicableNow " +
			                             "      and exists(select 1 from dbo.activeflag f where f.activeflagid=st.activeflagid and f.allowaccess=1) " +
			                             "  union " +
			                             "    select x.parentsecuritytokenid " +
			                             "    from security.securitytokenxsecuritytoken x " +
			                             "    join applicable a on x.childsecuritytokenid = a.securitytokenid " +
			                             "    join security.securitytoken parent on parent.securitytokenid=x.parentsecuritytokenid " +
			                             "    join classification.classification parenttype on parenttype.classificationid=parent.securitytokenclassificationid " +
			                             "    where x.enterpriseid = :applicableEnterprise " +
			                             "      and parent.enterpriseid=:applicableEnterprise and parenttype.enterpriseid=:applicableEnterprise and parenttype.classificationname<>'Plugin' " +
			                             "      and parent.effectivefromdate<=:applicableNow and parent.effectivetodate>:applicableNow " +
			                             "      and exists(select 1 from dbo.activeflag f where f.activeflagid=parent.activeflagid and f.allowaccess=1) " +
			                             "      and (x.effectivefromdate <= :applicableNow) " +
			                             "      and (x.effectivetodate > :applicableNow) " +
			                             "      and x.activeflagid in (:applicableVisible) " +
			                             ") ";
    }
}
