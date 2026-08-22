# Prompt Evaluation Platform

A cloud-neutral platform for versioning prompts, running repeatable evaluations, and comparing local LLM outputs before a prompt or model change reaches production.

> **Status:** private, early development. It will be published only after API, tests, documentation, local demo, and CI meet the release checklist.

## Problem

Prompt changes are software changes, but many teams cannot reproduce, compare, or approve them with the same discipline as code. This project makes prompt releases measurable with datasets, evaluation criteria, run history, and model comparisons.

## Planned capabilities

- Prompt templates with immutable versions
- Evaluation datasets and expected-result criteria
- Local/Ollama-compatible model adapters
- Repeatable evaluation runs with latency and token metrics
- Prompt/model comparison and release quality gates
- PostgreSQL persistence, Flyway, Prometheus metrics, Docker, Kubernetes, and React

## Architecture

The first release is a modular monolith. Its domain boundaries are prompt catalog, dataset management, execution, evaluation, and reporting. This keeps local setup simple while preserving future extraction paths.

```text
Prompt catalog + datasets -> evaluation runner -> local model adapter
                                      |                  |
                                 run history        normalized output
                                      |
                              comparison / quality gate
```

## Technology

Java 21, Spring Boot, PostgreSQL, React, Maven, Docker, Kubernetes, Prometheus, and optional Ollama. The local development path uses only free and open-source software.

## Engineering standards

- Issues, feature branches, and pull requests
- AI-assisted changes labeled explicitly
- CI before merge
- Minimum 70% line coverage
- No empty commits or synthetic activity

## License

[MIT](LICENSE)
