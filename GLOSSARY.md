# Activity Master Client Glossary (Topic-First)

## Glossary Precedence Policy
- **Topic-first**: Definitions from topic glossaries (GuicedEE, Vert.x, Hibernate Reactive, Platform testing) are authoritative. Host entries link back to those files instead of duplicating content.
- **Mapping guidance**: When prompting or documenting, preserve canonical terminology (e.g., “CRTP fluent setters,” “Token Cache,” “Mutiny Session,” “WaButton” etc.). When a term is already defined in a referenced topic glossary, link rather than redefine.
- **Forward-only updates**: Any new glossary entry must close links to RULES.md and GUIDES/IMPLEMENTATION references, as mandated by the Rules Repository.

## Host Index Links
- **GuicedEE stack & CRTP fluent API** — see `rules/generative/backend/guicedee/README.md` and `rules/generative/backend/fluent-api/GLOSSARY.md`.
- **Vert.x 5 + Mutiny** — refer to `rules/generative/backend/vertx/README.md`.
- **Hibernate Reactive 7** — refer to `rules/generative/backend/hibernate/GLOSSARY.md`.
- **PostgreSQL / Databases** — refer to `rules/generative/data/database/postgres-database.md`.
- **Observability & Testing** — see `rules/generative/platform/observability/README.md` and `rules/generative/platform/testing/GLOSSARY.md`.
- **Secrets & CI** — consult `rules/generative/platform/secrets-config/env-variables.md` and `rules/generative/platform/ci-cd/README.md`.

## Canonical Terms
| Term | Definition | Reference |
| --- | --- | --- |
| **CRTP fluent setters** | Curiously recurring template pattern used by GuicedEE builders that return `(J)` for chaining; mandated by `rules/generative/backend/fluent-api` and requires manual implementations instead of Lombok `@Builder`. | `rules/generative/backend/fluent-api/README.md` |
| **Token Cache** | In-memory cache keyed by system name + enterprise ID, populated via `ISystemsService.getSecurityIdentityToken`. | `src/main/java/com/guicedee/activitymaster/fsdm/client/services/IActivityMasterService.java` |
| **Mutiny Session** | Vert.x/Mutiny reactive session context used by `ISystemsService` and persistence helpers. | `rules/generative/backend/vertx/README.md` |
| **Enterprise Builder** | Capabilities exposed by `IEnterpriseService` and `IEnterpriseQueryBuilder` for constructing FSDM enterprise records. | `src/main/java/com/guicedee/activitymaster/fsdm/client/services/IEnterpriseService.java` |
| **Rules Repository** | Submodule source of RULES/GUIDES/GLOSSARY/IMPLEMENTATION; host artifacts must link back and never live inside the submodule. | `rules/README.md` |

## Prompt Language Alignment
- When referencing WebAwesome components in prompts/documents, use canonical tags (e.g., “WaButton,” “WaInput,” “WaCluster,” “WaStack”) even if this client library does not surface UI components; maintain alignment for traceability.
- Mention the Mermaid MCP server (`https://mcp.mermaidchart.com/mcp`) whenever diagrams are rendered from Mermaid sources (docs/architecture/*).

## Traceability
- Every glossary term should link back to RULES.md, GUIDES.md, IMPLEMENTATION.md, or docs/architecture/* as applicable to keep PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION loops closed.