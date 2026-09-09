# Prompt Evaluation Platform

A cloud-neutral platform for versioning prompts, running repeatable evaluations, and comparing local LLM outputs before a prompt or model change reaches production.

> **Status:** public work in progress. The core prompt-version, dataset, and deterministic evaluation-run APIs are available. This repository does not yet represent a production release.

## Release progress

![v0.1.0 release progress](docs/images/release-progress.svg)

The progress view is a manual, scope-based snapshot of the [v0.1.0 milestone](../../milestone/1); it is intentionally not derived from commit count or activity volume.

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

![Architecture diagram](docs/images/architecture.svg)

## Capability chart

![Capability implementation chart](docs/images/capability-chart.svg)


## Technology

Java 21, Spring Boot, PostgreSQL, React, Maven, Docker, Kubernetes, Prometheus, and optional Ollama. The local development path uses only free and open-source software.

## Complete local API workflow

Prerequisites are Java 21, a running PostgreSQL instance, `curl`, and `jq`. Create an empty
`prompt_eval` database, then start the API in one terminal (adjust the credentials for your local
PostgreSQL installation):

```sh
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/prompt_eval'
export SPRING_DATASOURCE_USERNAME='postgres'
export SPRING_DATASOURCE_PASSWORD='postgres'
./mvnw spring-boot:run
```

Flyway creates and migrates the tables on startup. In a second terminal, copy and paste this entire
block to create a template and its second immutable version, create a two-case dataset, execute that
version, and retrieve the persisted results:

```sh
set -eu
API='http://localhost:8080/api'

TEMPLATE=$(curl --fail-with-body -sS -X POST "$API/prompt-templates" \
  -H 'Content-Type: application/json' \
  -d '{"name":"ticket-classifier","content":"Legacy classification: {{ticket}}"}')
TEMPLATE_ID=$(printf '%s' "$TEMPLATE" | jq -r '.id')
printf '%s\n' "$TEMPLATE" | jq

VERSIONED_TEMPLATE=$(curl --fail-with-body -sS -X POST \
  "$API/prompt-templates/$TEMPLATE_ID/versions" \
  -H 'Content-Type: application/json' \
  -d '{"content":"Classify: {{ticket}}"}')
PROMPT_VERSION_ID=$(printf '%s' "$VERSIONED_TEMPLATE" | \
  jq -r '.versions[] | select(.version == 2) | .id')
printf '%s\n' "$VERSIONED_TEMPLATE" | jq

DATASET=$(curl --fail-with-body -sS -X POST "$API/evaluation-datasets" \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"ticket-cases",
    "cases":[
      {
        "inputVariables":{"ticket":"Cannot log in"},
        "expectedOutput":"Classify: Cannot log in"
      },
      {
        "inputVariables":{"ticket":"Invoice is wrong"},
        "expectedOutput":"Billing issue"
      }
    ]
  }')
DATASET_ID=$(printf '%s' "$DATASET" | jq -r '.id')
printf '%s\n' "$DATASET" | jq

RUN=$(curl --fail-with-body -sS -X POST "$API/evaluation-runs" \
  -H 'Content-Type: application/json' \
  -d "{\"promptVersionId\":\"$PROMPT_VERSION_ID\",\"datasetId\":\"$DATASET_ID\"}")
RUN_ID=$(printf '%s' "$RUN" | jq -r '.id')
printf '%s\n' "$RUN" | jq

curl --fail-with-body -sS "$API/evaluation-runs/$RUN_ID" | jq
```

The built-in deterministic local provider echoes each safely rendered prompt. Consequently, the
first case passes, the second fails, and both the create and retrieve responses report `COMPLETED`,
a `passRate` of `0.5`, per-case rendered prompts, provider outputs, pass/fail outcomes, and latency.
No external model or paid API is involved.

Template names and dataset names must be non-blank and at most 120 characters. Prompt content,
input values, and expected outputs are limited to 50,000 characters. A dataset requires at least one
case; cases and prompt versions are returned in their original order.

## Engineering standards

- Issues, feature branches, and pull requests
- AI-assisted changes labeled explicitly
- CI before merge
- Minimum 70% line coverage
- No empty commits or synthetic activity

## License

[MIT](LICENSE)
