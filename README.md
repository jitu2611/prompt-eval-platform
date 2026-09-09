# Prompt Evaluation Platform

A local-first reference application for versioning prompts, defining evaluation datasets, running deterministic evaluations, and inspecting persisted results.

> **Status:** the implemented v0.1.0 scope is complete. It is a local demonstration release, not a production-hosted service. It uses a built-in deterministic echo provider and does not call an LLM or external API.

## v0.1.0 at a glance

![v0.1.0 release status](docs/images/release-progress.svg)

The v0.1.0 workflow includes:

- prompt template creation, retrieval, and immutable version creation;
- evaluation dataset creation and retrieval with ordered test cases;
- synchronous evaluation runs against a deterministic local provider;
- exact-match pass/fail scoring, pass rate, output, and latency persistence;
- run retrieval through the API; and
- a browser dashboard that creates a prompt and dataset and displays run results.

Quick links: [dashboard demo](#dashboard-demo) · [API curl workflow](#api-curl-workflow) · [build and verification](#build-and-verification) · [v0.1.0 release notes](CHANGELOG.md#010)

## Architecture

v0.1.0 is a Spring Boot modular monolith backed by PostgreSQL. Flyway owns the schema, and the dashboard is static HTML, CSS, and JavaScript served by the same application as the API.

![Architecture diagram](docs/images/architecture.svg)

![Capability implementation chart](docs/images/capability-chart.svg)

## Prerequisites

Required to build and test:

- Java 21 (`java -version`)
- a POSIX shell on macOS/Linux, or Windows with `mvnw.cmd`
- internet access on the first Maven Wrapper run to download public Maven dependencies

Required to run the application:

- a reachable PostgreSQL instance and an empty database (the examples use `prompt_eval`)
- database credentials allowed to create and alter objects in that database so Flyway can migrate it

Required only for the API walkthrough: `curl` and `jq`.

No Node.js toolchain, model download, API key, Docker, or external service is required. The automated test suite uses H2 and does not require a running PostgreSQL instance.

## Build and verification

Run unit and integration tests:

```sh
./mvnw --batch-mode --no-transfer-progress test
```

Build the executable jar (including tests):

```sh
./mvnw --batch-mode --no-transfer-progress package
```

The artifact is `target/prompt-eval-platform-0.1.0.jar`.

Run the full release-readiness check, including the 70% line-coverage gate:

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

## Run locally

Create an empty PostgreSQL database named `prompt_eval` using your preferred PostgreSQL administration tool. Then set the connection details and start the application:

```sh
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/prompt_eval'
export SPRING_DATASOURCE_USERNAME='postgres'
export SPRING_DATASOURCE_PASSWORD='postgres'
./mvnw spring-boot:run
```

Flyway creates or upgrades the application tables during startup. Keep this terminal running while using either workflow below. To run the packaged artifact instead, use the same environment variables and:

```sh
java -jar target/prompt-eval-platform-0.1.0.jar
```

### Dashboard demo

1. Open [http://localhost:8080/](http://localhost:8080/) after startup.
2. Keep the prefilled prompt and two test cases, or edit them.
3. Select **Run deterministic evaluation**.
4. Inspect the pass rate, provider output, expected output, and per-case latency.

The prefilled demo intentionally produces one passing and one failing case. Every click creates and persists a new prompt template, dataset, run, and case results in the configured local database.

### API curl workflow

In a second terminal, run this complete workflow to create a prompt template and a second immutable version, create a two-case dataset, execute it, and retrieve its persisted results:

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

The provider returns each safely rendered prompt unchanged. The first case therefore passes and the second fails. Both run responses report `COMPLETED`, a `passRate` of `0.5`, and ordered case results containing rendered prompts, provider outputs, exact-match outcomes, and measured latency.

Template and dataset names must be non-blank and at most 120 characters. Prompt content, input values, and expected outputs are limited to 50,000 characters. A dataset requires at least one case; cases and prompt versions are returned in creation order.

## Scope boundaries

v0.1.0 does **not** include authentication, a hosted deployment, an external or Ollama model integration, semantic/model-based scoring, prompt or model comparison, configurable quality gates, streaming/asynchronous runs, or Docker/Kubernetes packaging. These are not implied by the release status graphics.

## Engineering standards

- Issues, feature branches, and pull requests
- AI-assisted changes labeled explicitly
- CI before merge
- Minimum 70% line coverage
- No empty commits or synthetic activity

## License

[MIT](LICENSE)
