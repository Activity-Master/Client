# C4 Context — Activity Master Client

```mermaid
C4Context
    Person(client_dev, "Client Developer", "Integrates the Activity Master Client library into downstream services")
    System(activity_master_client, "Activity Master Client (com.guicedee.activitymaster.fsdm.client)", "GuicedEE/Vert.x client exposing FSDM functionality through a reactive SPI")
    System_Ext(guicedee, "GuicedEE Platform", "Provides GuicedInjection, lifecycle hooks, and SPI discovery used by the client")
    System_Ext(vertx, "Vert.x 5 Event Loop", "Reactive runtime powering Mutiny/Vert.x sessions accessed by the client")
    System_Ext(hibernate_reactive, "Hibernate Reactive 7", "Non-blocking persistence layer (Mutiny + persistence.xml configuration)")
    System_Ext(postgres, "PostgreSQL Database", "Stores core FSDM entities (enterprises, systems, capabilities, tokens)")
    System_Ext(entity_assist, "Entity Assist Reactive", "GuicedEE-friendly ORM patterns used to model the FSDM domain")
    System_Ext(rules_repo, "Rules Repository Submodule", "adopts canonical RULES/GUIDES/GLOSSARY/IMPLEMENTATION for alignment")

    Person(client_dev) -> System(activity_master_client, "Injects via GuicedInjection, accesses IActivityMasterService APIs")
    System(activity_master_client) -> System_Ext(guicedee, "Uses GuicedInjection, IGuicePreStartup, module inclusions, and logging")
    System(activity_master_client) -> System_Ext(vertx, "Executes Mutiny Sessions and ISystemsService/ISystems operations")
    System(activity_master_client) -> System_Ext(hibernate_reactive, "Persists entities with Mutiny SessionFactory wiring")
    System(activity_master_client) -> System_Ext(postgres, "Reads/writes FSDM domain tables (enterprises, tokens)")
    System(activity_master_client) -> System_Ext(entity_assist, "Follows Entity Assist reactive CRUD patterns for domain modeling")
    System(activity_master_client) -> System_Ext(rules_repo, "References RULES/GUIDES/GLOSSARY/IMPLEMENTATION for policy compliance")
```

## Narrative
- The **Activity Master Client** is a GuicedEE-aware library that exports a rich service API surface (`com.guicedee.activitymaster.fsdm.client.services.*`), including CRTP-driven fluent setters, reactive Uni-returning SPI operations, and system/token cache helpers.
- It relies on **GuicedEE** for dependency injection, service discovery, and lifecycle contributions (`ActivityMasterClientModuleInclusion`, `ActivityMasterClientPreStartup`).
- The runtime stack uses **Vert.x 5** event loops and **Hibernate Reactive 7** to ensure non-blocking access to the **PostgreSQL** implementation of the Financial Services Data Model (FSDM).
- This host repository consumes the **Rules Repository submodule** to stay aligned with enterprise-wide RULES/GUIDES/GLOSSARY/IMPLEMENTATION artifacts and closes loops between policy and execution.