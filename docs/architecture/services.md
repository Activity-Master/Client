# IxxxService Contracts

## Usage conventions
- Each `I…Service` is the SPI entrypoint for its entity family and exposes a default `get()` that returns the EntityAssist-backed query/builder already wired with the caller’s session, enterprise/system context, and default flags (identity token varargs stay optional).
- Callers always supply the `Mutiny.Session` they control; services never create sessions or transactions on your behalf and stay reactive (no blocking/await on the service surface).
- Reads and writes flow through the services; the `IManage…` helper interfaces keep circular relationships in the canonical schema coherent, while `IContains…` declares the shared column sets and default behaviors.
- Token caches and other convenience layers (for example `IActivityMasterService.SYSTEM_TOKEN_CACHE`) sit behind the services so external clients never touch implementation classes—only the interfaces in `src/main/java/com/guicedee/activitymaster/fsdm/client/services`.

## Service catalog
- **IActivityMasterService** — orchestrates system bootstrap (`loadSystems`, `loadUpdates`), hands out cached system tokens (see `sequence-system-token.md`), and routes system lookups to `ISystemsService`.
- **ISystemsService** — owns system definitions and security identities: create/register systems, find by enterprise/name, and fetch security tokens used by the cache.
- **IEnterpriseService** — lifecycle for enterprises (create/start new, readiness checks, apply updates, post-startup hooks) and lookup by UUID/name.
- **IActiveFlagService** — supplies canonical active/archived/deleted/highlighted flags and visibility ranges for an enterprise.
- **ITimeService** — pure helper for deriving day/hour/minute IDs used by the warehouse time dimensions.
- **IClassificationDataConceptService** — defines and queries classification data concepts (global/no concept/security hierarchy) attached to classification trees.
- **IClassificationService** — builds and traverses enterprise classification hierarchies with optional concepts and parent/child sequencing; provides typed helpers for Enum-based creation and lookup.
- **IRulesService** — creates rule records and rule types, links them to products/resource items/classifications, and resolves rules by type or product.
- **ISecurityTokenService** — enforces row-level security: every canonical table row carries a security token and every service method’s trailing `UUID... identityToken` varargs are evaluated to ensure the caller’s token can read/write that row. The service manages the token hierarchy/folders, grants CRUD permissions between tokens, and resolves well-known groups (everyone/everywhere/administrators/etc.). Tokens are attached per-row (and effectively per-cell for sensitive fields) to gate reads and writes.
- **IPasswordsService** — user credential surface on top of involved parties: find by username/password, create/update credentials, seed admin/creator users for an enterprise.
- **IInvolvedPartyService** — entrypoint for people/orgs; creates party types/name types/identification types, and finds parties by tokens, resource items, classifications, or identification values. Use `get()` to obtain the involved-party query builder.
- **IAddressService** — manages contact/address artifacts (IP, host, web, phone, email, street, postal) and lookups for party-linked contacts; throws `AddressException` on validation failures.
- **IResourceItemService** — handles resource items and their data payloads: create item types, create/find items (with optional binary data and original source IDs), update data, and resolve items by classification, type, or UUID.
- **IProductService** — product and product-type lifecycle: create/find products, attach resource items, resolve product types by classification, and list products by type.
- **IArrangementsService** — arrangement and arrangement-type lifecycle: create arrangements (typed/classified), find by involved party/resource item/rules/classification with temporal filters, and complete arrangements.
- **IEventService** — event capture layer: create/find events and event types tied to systems.
- **IActivityMasterProgressMonitor / event hooks (IOnSystemInstall/IOnSystemUpdate/IOnCreateUser/IOnExpireUser)** — optional cross-cutting callbacks surfaced as services for progress reporting and lifecycle events; consumed during enterprise/system setup. Interceptors such as `IOnSystemUpdate` run during the registration/update cycle for systems so you can attach side effects (logging, migration, auditing) to system updates.
- **Default classifications** — the built-in classification enums live under `com.guicedee.activitymaster.fsdm.client.services.classifications` and its subpackages (e.g., addresses, arrangements, products). Use these enums with `IClassificationService` to seed or reference standard classification values.

## How to consume
1) Resolve the service from Guice (`IGuiceContext.get(IInvolvedPartyService.class)`, etc.).
2) Open/pass a `Mutiny.Session` from the host application.
3) Use `service.get()` when you need a query/builder, and the service methods when you need writes or higher-level reads.
4) Provide the `ISystems` and optional identity tokens to keep tenant/security scope explicit. See `sequence-system-load.md` for session + system loading flow and `sequence-system-token.md` for token caching.

### Bootstrap template (tests or setup)
Install enterprise + Activity Master system (idempotent inside a transaction):
```java
sessionFactory.withSession(session ->
    session.withTransaction(tx -> {
        var enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        var systemsService = IGuiceContext.get(ISystemsService.class);
        return enterpriseService.getEnterprise(session, TestEnterprise.name())
            .onFailure().recoverWithUni(t -> {
                var ent = enterpriseService.get();
                ent.setName(TestEnterprise.name());
                ent.setDescription("Enterprise Entity for Lifecycle Testing");
                return enterpriseService.createNewEnterprise(session, ent);
            })
            .chain(ent -> systemsService.getActivityMaster(session, ent)
                .onFailure().recoverWithUni(t -> systemsService.create(session, ent,
                    ISystemsService.ActivityMasterSystemName, "Activity Master System")))
            .replaceWith(Uni.createFrom().voidItem());
    })
).await().atMost(Duration.of(2, ChronoUnit.MINUTES));
```
Start the enterprise in a separate session (still idempotent):
```java
sessionFactory.withSession(session ->
    session.withTransaction(tx -> {
        var enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        return enterpriseService.startNewEnterprise(session, TestEnterprise.name(), "admanastrator", "!12345!")
            .onFailure().recoverWithItem(e -> null)
            .replaceWith(Uni.createFrom().voidItem());
    })
).await().atMost(Duration.of(2, ChronoUnit.MINUTES));
```
Typical `@BeforeAll` pulls both blocks together so later tests run against a ready enterprise + Activity Master system.

### Register your application as a system
Implement `IActivityMasterSystem` to register your app/system and obtain its identity token. Implementations must supply:
- `getSystemName()` (reused everywhere),
- `getSortOrder()` (load order in the startup chain),
- `getDescription()`.
Use `postStartup` to verify the system exists and its token is reachable; create if missing:
```java
@Override
public Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    log.info("Starting reactive postStartup for Address System");
    return systemsService.findSystem(session, enterprise, getSystemName())
        .onItem().ifNull().failWith(() -> new RuntimeException("System not found: " + getSystemName()))
        .chain(system -> systemsService.getSecurityIdentityToken(session, system)
            .onItem().ifNull().failWith(() -> new RuntimeException("Security token not found for system: " + system.getName()))
            .replaceWith(Uni.createFrom().voidItem()));
}
```
Register and create the token via `registerSystem`:
```java
@Inject ISystemsService<?> systemsService;

@Override
public Uni<ISystems<?, ?>> registerSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    return systemsService.create(session, enterprise, AddressSystemName, "The system for address management")
        .chain(system -> getSystem(session, enterprise)
            .chain(sys -> systemsService.registerNewSystem(session, enterprise, sys))
            .replaceWith(system));
}
```
Run both in `@BeforeAll` or startup hooks so your application/system is installed and has its security token before other services execute.

Provide system defaults (event types, classifications, data concepts) in `createDefaults`—these hooks are invoked externally and should stay independent of each other:
```java
@Override
public Uni<Void> createDefaults(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    logProgress("Address System", "Starting Address Checks");
    log.info("Creating address defaults for enterprise: '{}'", enterprise.getName());
    return Uni.createFrom().voidItem()
        .invoke(() -> log.info("Completed Address System defaults"))
        .onFailure().invoke(err -> log.error("Error in Address System defaults: {}", err.getMessage(), err))
        .replaceWithVoid();
}
```
Each override (`postStartup`, `registerSystem`, `createDefaults`, etc.) is called by the framework and should not call one another directly—keep them independent and idempotent.

### Retrieve the Activity Master system (method-driven)
Use the service helpers instead of hand-wiring lookups:
```java
private Uni<ISystems<?, ?>> getActivityMasterSystem(Mutiny.Session session) {
    var enterpriseService = IGuiceContext.get(IEnterpriseService.class);
    var systemsService = IGuiceContext.get(ISystemsService.class);
    return enterpriseService.getEnterprise(session, TestEnterprise.name())
        .chain(ent -> systemsService.getActivityMaster(session, ent));
}
```
Then fetch the token with `IActivityMasterService.getISystemToken(session, ISystemsService.ActivityMasterSystemName, enterprise)` to keep chains shallow and rely on the method-driven helpers.

### System updates (post-registration)
After a system is registered, `ISystemUpdate` implementations run to ensure the system is configured before start. Use `@SortedUpdate(sortOrder = X, taskCount = Y)` to control ordering, and keep implementations reactive:
```java
@Log4j2
@SortedUpdate(sortOrder = 709, taskCount = 1)
public class FarmStateJSONStorageUpdate implements ISystemUpdate {
    // reactive update steps here
}
```
These updates execute during the registration/update cycle; use them for migrations, seed data, or verification before the system goes live.
