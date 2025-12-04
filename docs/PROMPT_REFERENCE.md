# Prompt Reference — Activity Master Client

## Selected stacks (pinned for future prompts)
- **Language & Build**: Java 25 LTS + Maven (parent `activitymaster-group` BOM).
- **Injection & Lifecycle**: GuicedEE with `IGuiceScanModuleInclusions`, `IGuicePreStartup`, and Log4j2 logging instrumentation.
- **Reactive Runtime**: Vert.x 5 + Mutiny sessions driving the SPI operations.
- **Persistence**: Hibernate Reactive 7 layered over PostgreSQL (FSDM schema) with Entity Assist reactive builder integrations.
- **Fluent API Strategy**: CRTP per `rules/generative/backend/fluent-api/crtp.rules.md`; no Lombok `@Builder`.
- **Testing & Coverage**: Jacoco, BrowserStack, Java Micro Harness (per `rules/generative/platform/testing/README.md`).
- **Observability**: Health/readiness endpoints, logging best practices, and tracing per `rules/generative/platform/observability/README.md`.
- **Environment/Secrets**: Terraform/GitHub Actions supply the cataloged vars from `rules/generative/platform/secrets-config/env-variables.md`.

## Architecture artifacts
All diagrams rendered via the Mermaid MCP server (`https://mcp.mermaidchart.com/mcp`):
- `docs/architecture/README.md` — index linking every diagram.
- `docs/architecture/c4-context.md` — C4 Level 1 context view.
- `docs/architecture/c4-container.md` — C4 Level 2 container map.
- `docs/architecture/c4-component-client.md` — Level 3 view of SPI components.
- `docs/architecture/sequence-system-load.md` — sequence diagram for `loadSystems`.
- `docs/architecture/sequence-system-token.md` — token cache flow.
- `docs/architecture/erd-core-domain.md` — ERD covering enterprises, systems, and tokens.

## Glossary & prompt alignment
- Host glossary is topic-first (`GLOSSARY.md`). Prompts must respect canonical names like “Token Cache,” “Mutiny Session,” “WaButton,” etc.
- Link each glossary entry back to RULES/GUIDES/IMPLEMENTATION and, where relevant, to topic glossaries under `rules/`.

## Traceability
- Close loops: `PACT.md` ↔ `RULES.md` ↔ `GUIDES.md` ↔ `IMPLEMENTATION.md` ↔ `docs/architecture/*`.
- Always cite Mermaid MCP when discussing diagrams to honor the docs-as-code policy.
- Stage gates default to blanket approval for this run, so documentation flows from Stage 1 → Stage 2 → Stage 3 without STOP waits, but the process remains governed by the stage-gated protocol recorded above.

## MCP servers
- Mermaid MCP (`https://mcp.mermaidchart.com/mcp`) is registered for diagrams. Document any diagram source that uses it for future AI or developer references.