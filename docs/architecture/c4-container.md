# C4 Container — Activity Master Client

```mermaid
C4Container
    Person(client_dev, "Client Developer", "Integrates the Activity Master Client into bounded-context services")
    System_Boundary(activity_master_client_boundary, "Activity Master Client Module") {
        Container(client_library, "Activity Master Client", "Java 25 / Maven / GuicedEE + Vert.x", "Exports SPI surfaces such as `IActivityMasterService` and supporting service APIs.")
        Container(logic_container, "GuicedInjection / Lifecycle Host", "GuicedEE", "Discovers the client module, applies module inclusions, logging via Log4j2, and pre-startup hooks.")
        Container(runtime, "Vert.x 5 Event Loop", "Vert.x + Mutiny", "Executes reactive flows, such as `Mutiny.Session` and `ISystemsService` API calls.")
    }
    System_Ext(postgres, "PostgreSQL Database", "Stores FSDM domain tables (enterprise/system/token metadata)")
    System_Ext(hibernate_reactive, "Hibernate Reactive 7", "Provides non-blocking persistence backed by Mutiny SessionFactory")
    System_Ext(entity_assist, "Entity Assist Reactive", "Supplies entity hierarchies and builders used by the client APIs")
    System_Ext(rules_repo, "Rules Repository Submodule", "Provides RULES/GUIDES/GLOSSARY/IMPLEMENTATION references")

    Person(client_dev) -> Container(client_library, "Calls SPI APIs, configures token caching, and orchestrates system loads")
    Container(client_library) -> Container(runtime, "Runs Mutiny sessions, dispatches to `ISystemsService` and other services")
    Container(client_library) -> Container(logic_container, "Registers module inclusion, logging, and startup hooks (ActivityMasterClientModuleInclusion, ActivityMasterClientPreStartup)")
    Container(client_library) -> System_Ext(entity_assist, "Uses entity hierarchies (IEnterprise, ISystems, etc.) to load and persist FSDM data")
    Container(runtime) -> System_Ext(hibernate_reactive, "Executes reactive persistence operations for enterprises/systems/capabilities")
    Container(runtime) -> System_Ext(postgres, "Reads/writes core FSDM tables")
    Container(client_library) -> System_Ext(rules_repo, "Cross-links documentation artifacts to keep PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION traceable")
```

## Narrative
- The **Activity Master Client** module is built as a Maven Java 25 artifact that exports SPI interfaces (`com.guicedee.activitymaster.fsdm.client.services.*`) and fluent CRTP helpers, relying on GuicedEE for wiring and lifecycle integration.
- **GuicedInjection** discovers the module via `ActivityMasterClientModuleInclusion`, applies `ConsoleLogActivityMasterProgressMaster`, and invites pre-startup hooks (`ActivityMasterClientPreStartup`).
- **Vert.x 5 / Mutiny** powers the runtime, supplying `Mutiny.Session` contexts consumed by `ISystemsService`, `ISystems`, and `ISystemToken` helpers while the module backs data with **Hibernate Reactive 7** against **PostgreSQL**.
- This repository documents the system via the **Rules Repository submodule** to close the loop across RULES, GUIDES, GLOSSARY, and IMPLEMENTATION, ensuring compliance with the adopted governance model.