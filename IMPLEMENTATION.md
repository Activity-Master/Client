# Implementation Plan — Activity Master Client (Stage 3)

## Overview
This plan maps the host repository’s existing Maven module layout to the Stage 4 scaffolding steps while remaining documentation-first. All implementation actions iterate off the architecture artifacts (`docs/architecture/*`) and the prompt reference (`docs/PROMPT_REFERENCE.md`). The plan keeps the PACT → RULES → GUIDES → IMPLEMENTATION loop closed by cross-linking to those docs.

## Module Layout & Scaffolding
1. **`com.guicedee.activitymaster.fsdm.client` module**  
   - Contains SPI exports (`IActivityMasterService`, `IAgency`, etc.), builders (`services.builders.warehouse.*`), and implementations (`ActivityMasterClientModuleInclusion`, `ActivityMasterClientPreStartup`).  
   - Stage 4 scaffolding will validate that exported packages and service files align with the C4 component view (`docs/architecture/c4-component-client.md`).  
   - Implementation actions: ensure `module-info.java` re-exports exactly the SPI packages listed, add any necessary service providers, and keep token cache helpers traced in the Host RULES.

2. **Service & Builders directories**  
   - The directories under `src/main/java/com/guicedee/activitymaster/fsdm/client/services/` will host future concrete implementations, but the Stage 3 plan treats them as reference surfaces for design validation (see `GUIDES.md` for flow mapping).  
   - Any scaffolding (e.g., new builder implementations, tests, utilities) will follow the CRTP pattern from `rules/generative/backend/fluent-api/crtp.rules.md`.

## Build & Dependency Plan
- Build tool: Maven (root `pom.xml` inherits `activitymaster-group` parent).  
- Dependencies (GuicedEE injection, Vert.x 5, Mutiny, Entity Assist, Hibernate Reactive 7, Log4j2) are declared already; confirm future additions reference `rules/generative/language/java/build-tooling.md` to avoid duplication.  
- Stage 4 actions: add any required test dependencies (e.g., Testcontainers) with scope `test` and keep coordinates aligned with the parent BOM.

## Environment & Configuration
- `.env.example` documents all runtime and secret keys aligned with `rules/generative/platform/secrets-config/env-variables.md`.  
- Stage 4 will ensure GuicedEE/Vert.x configuration uses these env vars, logging via Log4j2, and observability per `rules/generative/platform/observability/README.md`.  
- Terraform or GitHub Actions must supply sensitive values such as `POSTGRES_APP_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD`, and `JWT_TEST_TOKEN`.

## CI & Validation
- GitHub Actions workflow (`.github/workflows/ci.yml`) reuses the shared GuicedEE workflow for Maven packaging and exposes required secrets (`USERNAME`, `USER_TOKEN`, `SONA_*`, `POSTGRES_APP_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD`, `JWT_TEST_TOKEN`).  
- Validation steps: run `mvn test` (Jacoco coverage), optionally trigger BrowserStack for UI tests, and verify Mermaid diagram renders via Mermaid MCP.  
- Document test strategy in `GUIDES.md` (sequence flows) and reference `rules/generative/platform/testing/README.md`.

## Rollout & Phased Deployment
1. **Phase 1** — Documentation alignment (done). Confirm Stage 1/2 docs and architecture diagrams remain in sync.
2. **Phase 2** — Implementation scaffolding: add any missing service implementations or configuration files while keeping architecture traceability intact (links to `docs/architecture/README.md`).  
3. **Phase 3** — Validation & release: run CI workflow, verify logs, and release the Maven artifact once coverage/observability checks pass.

## Risks & Validation Approaches
- **Risk**: Assumed persistence schema (enterprises/systems/tokens) drawn from service interfaces may drift from the actual database. Mitigation: align ERD (`docs/architecture/erd-core-domain.md`) with upstream schema before modifying persistence code.  
- **Risk**: CRTP fluent setters must stay consistent; avoid introducing Lombok `@Builder` artifacts. Validation: code reviews compare new fluent signatures to `rules/generative/backend/fluent-api/crtp.rules.md`.  
- **Risk**: Secrets/config drift (e.g., missing env keys). Validation: `.env.example` mirrors `rules/generative/platform/secrets-config/env-variables.md`, and GitHub Actions ensures each secret is declared.

## Forward Path
Stage 4 will introduce targeted implementation changes (new classes, config wiring, tests) tied directly to these documented plans. Each change will be accompanied by diagram updates (Mermaid via Mermaid MCP) and cross-links back to RULES/GUIDES/IMPLEMENTATION.