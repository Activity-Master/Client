# Client
Client Library for the Activity Master.

## Rules Repository Adoption
This repository consumes the [`rules/` submodule](rules/) as the canonical set of enterprise RULES, GUIDES, and GLOSSARY artifacts; host-specific docs (PACT/RULES/GUIDES/IMPLEMENTATION/GLOSSARY) live at the repo root and link back to the submodule.

## Documentation Home
- PACT — [`PACT.md`](PACT.md) defines this collaboration and stage gates.
- RULES — [`RULES.md`](RULES.md) declares scope, stack references, and traceability mandates.
- GLOSSARY — [`GLOSSARY.md`](GLOSSARY.md) follows the topic-first precedence policy and links to every referenced topic glossary.
- GUIDES — [`GUIDES.md`](GUIDES.md) explains how to apply the RULES and maps the API surface to the submodule topics.
- IMPLEMENTATION — [`IMPLEMENTATION.md`](IMPLEMENTATION.md) captures the module layout, diagrams, and commit-ready traceability.
- Architecture — [`docs/architecture/README.md`](docs/architecture/README.md) indexes the C4/sequence/ERD diagrams produced via the Mermaid MCP server.

## Architecture & Prompt Reference
Use the diagrams in `docs/architecture/` (context → container → component plus sequences/ERD) to understand the system boundaries before editing code. Every diagram is a Mermaid source rendered via `https://mcp.mermaidchart.com/mcp`. The prompt/tracing anchor lives in [`docs/PROMPT_REFERENCE.md`](docs/PROMPT_REFERENCE.md).

## Getting Started
- Add the Maven artifact `com.guicedee.activitymaster:activity-master-client` (see `pom.xml`) to your GuicedEE module.
- Register the client via `ActivityMasterClientModuleInclusion` and rely on `IActivityMasterService` plus the supporting service APIs (`ISystemsService`, `IEnterpriseService`, etc.).
- Cache tokens through the built-in `SYSTEM_TOKEN_CACHE` helper and respect CRTP fluent setters when chaining builders.

## Configuration
- Copy `.env.example` as your local `.env` file; keep the keys and prefixes aligned with `rules/generative/platform/secrets-config/env-variables.md`.
- Inject PostgreSQL credentials, token secrets, and logging toggles via Terraform or GitHub Actions secrets before deployment.

## CI & Secrets
- GitHub Actions (see `.github/workflows/ci.yml`) wires the shared GuicedEE workflow and expects secrets such as `USERNAME`, `USER_TOKEN`, `SONA_USERNAME`, `SONA_PASSWORD`, `POSTGRES_APP_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD`, and `JWT_TEST_TOKEN`.
- Jacoco/BrowserStack coverage, health endpoints, and Log4j2 configuration all follow their respective rule guide links inside `rules/generative/platform/`.

## Stage Gates & Diagrams
Blanket approval was granted for this run, so documentation stages proceed without STOP waits. Always reference the Mermaid MCP server when discussing diagrams to maintain traceability from PACT ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION ↔ `docs/architecture/*`.
