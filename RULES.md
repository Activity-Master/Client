# Activity Master Client Rules

## Scope
- Java 25 (LTS) Maven library exposing `com.guicedee.activitymaster.fsdm.client` SPI surfaces (CRTP fluent builders, service interfaces, caching helpers, and GuicedEE lifecycle bindings).
- GuicedEE + Vert.x 5 + Hibernate Reactive 7 stack with PostgreSQL persistence and Log4j2 logging.
- Follows the Rules Repository for enterprise architecture, testing, observability, secrets, and prompt-language alignment; do not modify files under `rules/`, only reference them.

## Chosen Stacks & References
- **Fluent API strategy**: CRTP mandated by GuicedEE/GuicedInjection (`rules/generative/backend/fluent-api/README.md`, `rules/generative/backend/fluent-api/crtp.rules.md`, `rules/generative/backend/lombok/GLOSSARY.md`).
- **Dependency injection & modules**: `rules/generative/backend/guicedee/README.md`, `rules/generative/backend/guicedee/vertx/README.md`, `rules/generative/backend/guicedee/client/README.md`, plus the GuicedEE persistence glossary (`rules/generative/backend/guicedee/persistence/GLOSSARY.md`).
- **Reactive runtime**: Vert.x 5 (`rules/generative/backend/vertx/README.md`) with Mutiny sessions and PostgreSQL connectors.
- **Persistence**: Hibernate Reactive 7 (`rules/generative/backend/hibernate/README.md`, `rules/generative/backend/hibernate/hibernate-7-reactive.md`) layered over PostgreSQL (`rules/generative/data/database/postgres-database.md`).
- **Testing & coverage**: Jacoco (`rules/generative/platform/testing/jacoco.rules.md`), BrowserStack (`rules/generative/platform/testing/browserstack.rules.md`), Java Micro Harness, and the testing overview (`rules/generative/platform/testing/README.md`).

## Build & Tooling
- Build with Maven (adopt `rules/generative/language/java/build-tooling.md` for plugin guidance) and rely on the centralized BOM declared in the parent POM.
- Use the GuicedEE shared workflow for injection where possible; customize only when the maintainer explicitly approves new YAML.

## Logging & Observability
- Log4j2 is the default logger (`rules/generative/backend/logging/README.md`, `rules/generative/backend/logging/LOGGING_RULES.md`); wire configuration via `log4j2.xml` or system properties.
- Observability must align with `rules/generative/platform/observability/README.md`, including standardized health endpoints and diagnostics.

## Environment & Secrets
- Follow the env var catalog (`rules/generative/platform/secrets-config/env-variables.md`); `.env.example` (host) must mirror keys, Terraform is the canonical injector.
- Secret handling via GitHub Actions/environment variables (`POSTGRES_APP_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD`, `JWT_TEST_TOKEN`, etc.).

## Glossary & Prompt Alignment
- Host GLOSSARY (this repo) links to topic glossaries, obeys topic-first precedence, and documents enforced prompt-language mappings such as “CRTP fluent setters” and “TokenCache” terminology.
- Closure mandates: PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION ↔ `docs/architecture/*`.
- Document Modularity / Forward-Only: remove legacy monoliths, update all links, and never retain duplicate anchors.

## Documentation Traceability
- Link architecture docs (`docs/architecture/*`), diagrams (Mermaid via Mermaid MCP), and Prompt Reference (`docs/PROMPT_REFERENCE.md`).
- Close loops between these artifacts before authoring code, per the documentation-first, stage-gated workflow.