# Sequence — System Load Flow

```mermaid
sequenceDiagram
    participant ClientDev as Client Developer
    participant GuicedInjection as GuicedInjection
    participant ActivityMasterService as IActivityMasterService
    participant SystemsService as ISystemsService
    participant EntityAssist as Entity Assist Reactive
    participant VertxSession as Mutiny.Session

    ClientDev->>GuicedInjection: Include ActivityMasterClient module
    GuicedInjection->>ActivityMasterService: Obtain IActivityMasterService SPI
    ActivityMasterService->>VertxSession: Open session and request enterprise context
    VertxSession->>EntityAssist: Load enterprise/system definitions
    EntityAssist-->>VertxSession: Return enterprise/system entities
    ActivityMasterService->>SystemsService: Trigger loadSystems with session + enterprise
    SystemsService->>VertxSession: Query systems table (non-blocking via Hibernate Reactive)
    VertxSession-->>SystemsService: Emit retrieved systems
    SystemsService-->>ActivityMasterService: Deliver systems into client builders
    ActivityMasterService->>ClientDev: Notify completion of system load
```

## Narrative
1. The client developer wires the Activity Master Client module into GuicedInjection via `ActivityMasterClientModuleInclusion`.
2. GuicedInjection provides the `IActivityMasterService` SPI to host components.
3. Calls to `loadSystems` open `Mutiny.Session`, fetch enterprise context via Entity Assist-managed entities, and delegate to `ISystemsService`.
4. `ISystemsService` executes reactive queries (Hibernate Reactive 7 against PostgreSQL), streaming systems back into the client library.
5. The client library builds domain-specific objects and signals completion; token caches stay in sync for later operations.