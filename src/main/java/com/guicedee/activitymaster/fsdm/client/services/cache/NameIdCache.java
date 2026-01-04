package com.guicedee.activitymaster.fsdm.client.services.cache;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very lightweight in-memory cache for resolving frequently used name→ID lookups.
 * Targets reactive flows by returning Uni while keeping a simple TTL-based eviction.
 */
public final class NameIdCache {
    private static final Map<Key, Entry> CACHE = new ConcurrentHashMap<>();

    // Defaults can be tuned via setters if needed later (kept simple per request)
    private static volatile long ttlMillis = Duration.ofMinutes(5).toMillis();
    private static volatile int maxEntries = 10_000;

    private NameIdCache() {}

    public static void configure(Duration ttl, int maxSize) {
        if (ttl != null) ttlMillis = ttl.toMillis();
        if (maxSize > 0) maxEntries = maxSize;
    }

    public static Uni<UUID> getEnterpriseId(Mutiny.Session session,
                                            String enterpriseName,
                                            Resolver resolver) {
        String norm = normalize(enterpriseName);
        Key key = new Key("enterprise", norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        // Miss → resolve and populate
        return resolver.resolve(session, enterpriseName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve ActiveFlag ID by (enterpriseId, flagName) with caching.
     */
    public static Uni<UUID> getActiveFlagId(Mutiny.Session session,
                                            UUID enterpriseId,
                                            String flagName,
                                            Resolver resolver) {
        String norm = normalize(flagName);
        String domainKey = "activeflag|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, flagName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve Systems ID by (enterpriseId, systemName) with caching.
     */
    public static Uni<UUID> getSystemId(Mutiny.Session session,
                                        UUID enterpriseId,
                                        String systemName,
                                        Resolver resolver) {
        String norm = normalize(systemName);
        String domainKey = "systems|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, systemName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve Classification Data Concept ID by (enterpriseId, systemId, conceptName) with caching.
     */
    public static Uni<UUID> getClassificationDataConceptId(Mutiny.Session session,
                                                           UUID enterpriseId,
                                                           UUID systemId,
                                                           String conceptName,
                                                           Resolver resolver) {
        String norm = normalize(conceptName);
        String domainKey = "cdc|" + (enterpriseId == null ? "" : enterpriseId.toString()) + "|" + (systemId == null ? "" : systemId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, conceptName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve Classification ID by (enterpriseId, systemId, conceptId, classificationName) with caching.
     * Any of systemId or conceptId may be null to allow broader lookups; the cache key will reflect nulls.
     */
    public static Uni<UUID> getClassificationId(Mutiny.Session session,
                                                UUID enterpriseId,
                                                UUID systemId,
                                                UUID conceptId,
                                                String classificationName,
                                                Resolver resolver) {
        String norm = normalize(classificationName);
        String domainKey = "classification|" +
                (enterpriseId == null ? "" : enterpriseId.toString()) + "|" +
                (systemId == null ? "" : systemId.toString()) + "|" +
                (conceptId == null ? "" : conceptId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, classificationName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve ArrangementType ID by (enterpriseId, arrangementTypeName) with caching.
     */
    public static Uni<UUID> getArrangementTypeId(Mutiny.Session session,
                                                 UUID enterpriseId,
                                                 String arrangementTypeName,
                                                 Resolver resolver) {
        String norm = normalize(arrangementTypeName);
        String domainKey = "arrangementtype|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, arrangementTypeName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve EventType ID by (enterpriseId, eventTypeName) with caching.
     */
    public static Uni<UUID> getEventTypeId(Mutiny.Session session,
                                           UUID enterpriseId,
                                           String eventTypeName,
                                           Resolver resolver) {
        String norm = normalize(eventTypeName);
        String domainKey = "eventtype|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, eventTypeName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve ProductType ID by (enterpriseId, productTypeName) with caching.
     */
    public static Uni<UUID> getProductTypeId(Mutiny.Session session,
                                             UUID enterpriseId,
                                             String productTypeName,
                                             Resolver resolver) {
        String norm = normalize(productTypeName);
        String domainKey = "producttype|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, productTypeName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve ResourceItemType ID by (enterpriseId, resourceItemTypeName) with caching.
     */
    public static Uni<UUID> getResourceItemTypeId(Mutiny.Session session,
                                                  UUID enterpriseId,
                                                  String resourceItemTypeName,
                                                  Resolver resolver) {
        String norm = normalize(resourceItemTypeName);
        String domainKey = "resourceitemtype|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, resourceItemTypeName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve InvolvedPartyType ID by (enterpriseId, name) with caching.
     */
    public static Uni<UUID> getInvolvedPartyTypeId(Mutiny.Session session,
                                                   UUID enterpriseId,
                                                   String involvedPartyTypeName,
                                                   Resolver resolver) {
        String norm = normalize(involvedPartyTypeName);
        String domainKey = "involvedpartytype|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, involvedPartyTypeName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve InvolvedPartyNameType ID by (enterpriseId, name) with caching.
     */
    public static Uni<UUID> getInvolvedPartyNameTypeId(Mutiny.Session session,
                                                       UUID enterpriseId,
                                                       String involvedPartyNameTypeName,
                                                       Resolver resolver) {
        String norm = normalize(involvedPartyNameTypeName);
        String domainKey = "involvedpartynametype|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, involvedPartyNameTypeName)
                .invoke(id -> put(key, id));
    }

    /**
     * Resolve InvolvedPartyIdentificationType ID by (enterpriseId, name) with caching.
     */
    public static Uni<UUID> getInvolvedPartyIdentificationTypeId(Mutiny.Session session,
                                                                 UUID enterpriseId,
                                                                 String involvedPartyIdentificationTypeName,
                                                                 Resolver resolver) {
        String norm = normalize(involvedPartyIdentificationTypeName);
        String domainKey = "involvedpartyidentificationtype|" + (enterpriseId == null ? "" : enterpriseId.toString());
        Key key = new Key(domainKey, norm);

        UUID cached = getIfPresent(key);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }

        return resolver.resolve(session, involvedPartyIdentificationTypeName)
                .invoke(id -> put(key, id));
    }

    private static UUID getIfPresent(Key key) {
        Entry e = CACHE.get(key);
        if (e == null) return null;
        if (e.expiry < System.currentTimeMillis()) {
            CACHE.remove(key, e);
            return null;
        }
        return e.value;
    }

    private static void put(Key key, UUID value) {
        if (value == null) return;
        if (CACHE.size() >= maxEntries) {
            // simple coarse eviction: clear 10% oldest entries by timestamp scan
            CACHE.entrySet().stream()
                    .sorted((a,b) -> Long.compare(a.getValue().expiry, b.getValue().expiry))
                    .limit(Math.max(1, CACHE.size()/10))
                    .forEach(e -> CACHE.remove(e.getKey(), e.getValue()));
        }
        CACHE.put(key, new Entry(value, System.currentTimeMillis() + ttlMillis));
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    private record Key(String domain, String name) {
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(domain, key.domain) && Objects.equals(name, key.name);
        }
        @Override public int hashCode() { return Objects.hash(domain, name); }
    }

    private record Entry(UUID value, long expiry) {}

    @FunctionalInterface
    public interface Resolver {
        Uni<UUID> resolve(Mutiny.Session session, String name);
    }
}
