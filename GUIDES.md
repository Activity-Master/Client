# GUIDES for the Activity Master Client

## Rule Mapping & Discovery
- **RULES.md** declares the stack: Java 25, Maven, GuicedEE/Vert.x 5/Hibernate Reactive 7, PostgreSQL, Log4j2, CRTP, and the selected testing suite (Jacoco, BrowserStack, Java Micro Harness). Link back to `rules/generative/backend/guicedee/README.md`, `rules/generative/backend/vertx/README.md`, and `rules/generative/backend/hibernate/README.md` when extending these guides.
- **Glossary-driven guidance**: follow the topic-first priority defined in `GLOSSARY.md` to keep terminology consistent (Token Cache, Mutiny Session, Enterprise Builder, etc.).

## API Surface & Flows
1. **System lifecycle**: `IActivityMasterService` exposes `loadSystems`, `loadUpdates`, `runScript`, and static helpers such as `getISystem`/`getISystemToken`. Consult the implementation diagrams under `docs/architecture/c4-component-client.md` for how `ISystemsService`, `IEnterpriseService`, and the local token cache coordinate.
2. **Enterprise provisioning**: `IEnterpriseService` and `ActivityMasterConfiguration` manage enterprise lifecycle, including `startNewEnterprise`, `createNewEnterprise`, and `performPostStartup`. Link these operations to the persistence rules (`rules/generative/backend/guicedee/persistence/GLOSSARY.md`, `rules/generative/backend/hibernate/hibernate-7-reactive.md`).
3. **Event & capability patterns**: Service interfaces such as `IEventService`, `IManageProducts`, and `IManageRuleTypes` rely on builders defined in `src/main/java/com/guicedee/activitymaster/fsdm/client/services/builders/*`. Consult `rules/generative/backend/guicedee/functions/` for CRTP patterns and prompt alignments.

## Configuration & Environment
- Mirror `.env.example` with the keys from `rules/generative/platform/secrets-config/env-variables.md`; treat Terraform/GitHub Actions as authoritative for secrets injection.
- Document environment validation in `IMPLEMENTATION.md` by referencing the `rules/generative/platform/platform/observability/README.md` for health endpoints and the secrets guide for proxies.

## Testing & Validation
- Use Jacoco/BrowserStack instrumentation defined in `rules/generative/platform/testing/README.md`. Build tests around the API flows captured in `docs/architecture/sequence-system-load.md` and `docs/architecture/sequence-system-token.md`.
- Document the GuicedEE shared workflow (see `.github/workflows/ci.yml`) so implementers know where to supply `USERNAME`, `USER_TOKEN`, `SONA_*`, `POSTGRES_APP_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD`, and `JWT_TEST_TOKEN`.

## Diagrams & Traceability
- Always cite diagrams from `docs/architecture/README.md` (C4 context/container/component plus sequences and ERD). Mention the Mermaid MCP server `https://mcp.mermaidchart.com/mcp` whenever describing diagrams to maintain traceability.
- Close loops: PACT ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION ↔ `docs/architecture/*` ↔ `docs/PROMPT_REFERENCE.md`.

## Prompt Reference
- Reference `docs/PROMPT_REFERENCE.md` when engineering prompts for this project. It captures the selected stacks, diagrams, and glossary composition used by all assistive AI engines.