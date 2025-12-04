# C4 Component — Client Services Bounded Context

```mermaid
C4Component
    Container(activity_master_client, "Activity Master Client Library", "Java 25 / Maven", "Provides the `IActivityMasterService` SPI and supporting services for enterprises, systems, and capability lookups.")
    Component(i_activity_master_service, "IActivityMasterService<J>", "Service SPI", "Exposes reactive operations: load systems/updates, script execution, and token caches; implemented by SPI binder in host applications.")
    Component(i_systems_service, "ISystemsService<T>", "Reactive DAO service", "Handles system retrievals and security tokens using `Mutiny.Session` + Hibernate Reactive.")
    Component(i_event_service, "IEventService", "Event producer consumer", "Publish/subscribe for system event lifecycle notifications.")
    Component(token_cache, "System Token Cache", "In-memory cache + Mutiny pipeline", "Caches UUID tokens keyed by system name + enterprise ID; calls `ISystemsService.getSecurityIdentityToken` when miss.")
    Component(i_enterprise_service, "IEnterpriseService", "Enterprise loader", "Fetches enterprise definitions for the client builder APIs.")
    System_Ext(rules_repo, "Rules Repository Submodule", "Provides AUTHORITATIVE RULES, GUIDES, GLOSSARY, and IMPLEMENTATION references.")

    Container(activity_master_client) -> Component(i_activity_master_service, "Defines SPI exposed to host")
    Component(i_activity_master_service) -> Component(i_systems_service, "Reads systems, tokens, and caches via Mutiny")
    Component(i_activity_master_service) -> Component(i_enterprise_service, "Validates enterprise context builders")
    Component(i_activity_master_service) -> Component(token_cache, "Manages cached tokens for repeated flows")
    Component(token_cache) -> Component(i_systems_service, "Requests tokens on cache miss")
    Container(activity_master_client) -> Component(i_event_service, "Triggers event hooks on lifecycle changes")
    ActivityMasterClientModuleInclusion --> Container(activity_master_client, "Module inclusion via GuicedInjection (documented in RULES)")
    System_Ext(rules_repo) --> Container(activity_master_client, "Documents design, APIs, and traceability links")
```

## Narrative
- The Client Services bounded context revolves around `IActivityMasterService`, a CRTP-style SPI that orchestrates enterprise/system loading, script execution, and token retrieval while exposing builder helpers for configuration.
- Supporting services such as `ISystemsService`, `IEnterpriseService`, and `IEventService` interact with the Mutiny/Hibernate Reactive stack to source domain data, keeping the SPI reactive and non-blocking.
- Token caching is implemented as a local cache keyed by system name and enterprise ID; it delegates to `ISystemsService.getSecurityIdentityToken` when the cache misses.
- Module inclusion (`ActivityMasterClientModuleInclusion`) and pre-startup hooks (`ActivityMasterClientPreStartup`) keep the library wired into GuicedEE lifecycles, and the entire architecture links back to the Rules Repository artifacts for compliance and documentation traceability.