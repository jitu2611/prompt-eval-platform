# Changelog

This file records user-visible changes to the project. A changelog entry does not imply that a Git tag or GitHub release has been published.

## [0.1.0]

First local demonstration release.

### Added

- Prompt template creation and retrieval with immutable versions.
- Ordered evaluation datasets with input variables and expected outputs.
- Synchronous evaluation runs using a built-in deterministic echo provider.
- Persisted exact-match results, pass rates, rendered prompts, outputs, and latency.
- Browser dashboard and complete API curl workflow for the local demo.
- PostgreSQL schema management through Flyway and a verified Maven build with a 70% line-coverage gate.

### Known limitations

- Local, single-instance workflow with no authentication or hosted deployment.
- No external LLM/Ollama adapter, semantic scoring, comparisons, or configurable release quality gates.
