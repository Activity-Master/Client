---
version: 2.1
date: 2025-12-03
title: Activity Master Client — Human–AI Collaboration Pact
project: Activity Master Client
authors: [Activity Master Maintainers, Roo]
---

# 🤝 Activity Master Client Pact  
*(Human × AI Collaboration for the Client Library)*

## 1. Purpose  
This pact aligns Roo’s work with the Activity Master Client’s governance: adopt the Rules Repository, keep documentation modular, and proceed in Forward-Only mode. We stay specification-driven—PACT → RULES → GUIDES → IMPLEMENTATION—while closing loops with diagrams and prompt references.

## 2. Principles  

### 📚 Traceability  
- Every artifact links to RULES.md and its corresponding guide/implementation.  
- Architecture/content loops close with docs/architecture/* and docs/PROMPT_REFERENCE.md.

### 🛠️ Documentation-First  
- Stage 1/2 deliverables (architecture + guides) appear before implementation bits.  
- All diagrams live in `docs/architecture/` as Mermaid sources tied to Mermaid MCP.

### 🔁 Forward-Only Flow  
- Replace monoliths with modular docs; update or retire legacy references.  
- Document risky removals in MIGRATION.md if needed.

### 🧭 Rule Adherence  
- RULES.md declares CRTP fluent API, Vert.x 5, Hibernate Reactive 7, GuicedEE, PostgreSQL, Log4j2, and Jacoco/BrowserStack coverage.  
- Glossary execution follows topic-first precedence; host definitions link back to topic glossaries.

## 3. Structure of Work  

| Layer | Artifact |
| --- | --- |
| Pact (this file) | `PACT.md` |
| Rules | `RULES.md` (host) linking to `rules/` topics |
| Guides | `GUIDES.md` linking to technology-specific advice |
| Implementation | `IMPLEMENTATION.md` tracing the module layout and architectural docs |

## 4. Collaboration Notes  
- Assume blanket approval for stage gates; document it here and proceed.  
- Mention Mermaid MCP usage when referencing diagrams.  
- Keep doc updates staged: architecture → guides → plan → scaffolding.

## 5. Closing Loop  
- This pact references:
  - Architecture diagrams under `docs/architecture/`
  - Prompt reference guide `docs/PROMPT_REFERENCE.md`
  - MODULE definitions in `src/main/java`
  - RULES/GUIDES/IMPLEMENTATION describing how to apply them