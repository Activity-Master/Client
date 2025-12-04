# Architecture Index
Architecture artifacts live under `docs/architecture/` and are produced as modular Mermaid sources rendered against the [`Mermaid MCP server`](https://mcp.mermaidchart.com/mcp). Each file contains diagrams plus narrative that closes the traceability loop from CODE ƒ+' RULES ƒ+' GUIDES ƒ+' IMPLEMENTATION.

## Diagram artifacts
- [C4 Context](docs/architecture/c4-context.md) ƒ?" Level 1 view showing the Activity Master Client library and its external dependencies.
- [C4 Container](docs/architecture/c4-container.md) ƒ?" Level 2 view detailing the runtime containers, database, and dependency injectors.
- [C4 Component: Client Services](docs/architecture/c4-component-client.md) ƒ?" Level 3 view for the critical bounded context around `IActivityMasterService` and SPI components.
- [System Load Sequence](docs/architecture/sequence-system-load.md) ƒ?" Key server flow that initializes systems for an enterprise.
- [System Token Sequence](docs/architecture/sequence-system-token.md) ƒ?" Token caching and retrieval workflow that guards service access.
- [ERD: Core Domain](docs/architecture/erd-core-domain.md) ƒ?" Principal entities, relationships, and ownership for enterprises, systems, and capabilities.
- [Service Contracts](docs/architecture/services.md) ƒ?" Catalog of every `IxxxService` entrypoint, how EntityAssist builders are obtained, and how callers pass sessions/identity tokens.

## Traceability notes
Each diagram links backward to the relevant rules and forward to the implementation docs, ensuring PACT ƒ+" GLOSSARY ƒ+" RULES ƒ+" GUIDES ƒ+" IMPLEMENTATION compliance as mandated by the Rules Repository.
