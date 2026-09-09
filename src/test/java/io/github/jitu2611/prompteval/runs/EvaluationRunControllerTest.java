package io.github.jitu2611.prompteval.runs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(EvaluationRunController.class)
class EvaluationRunControllerTest {

	@Autowired
	private WebTestClient client;

	@MockitoBean
	private EvaluationRunService service;

	@Test
	void createsACompletedEvaluationRunWithCaseResults() {
		UUID runId = UUID.randomUUID();
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		UUID caseId = UUID.randomUUID();
		Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
		Instant completedAt = Instant.parse("2026-01-01T00:00:01Z");
		EvaluationRunResponse response = new EvaluationRunResponse(
				runId,
				promptVersionId,
				datasetId,
				EvaluationRunStatus.COMPLETED,
				1.0,
				createdAt,
				completedAt,
				List.of(new EvaluationCaseResultResponse(
						UUID.randomUUID(), caseId, 1, "Classify: login", "Classify: login", 3, true)));
		when(service.create(any())).thenReturn(response);

		client.post().uri("/api/evaluation-runs")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"promptVersionId":"%s","datasetId":"%s"}
						""".formatted(promptVersionId, datasetId))
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().valueEquals("Location", "/api/evaluation-runs/" + runId)
				.expectBody()
				.jsonPath("$.status").isEqualTo("COMPLETED")
				.jsonPath("$.passRate").isEqualTo(1.0)
				.jsonPath("$.completedAt").isEqualTo(completedAt.toString())
				.jsonPath("$.results[0].evaluationCaseId").isEqualTo(caseId.toString())
				.jsonPath("$.results[0].renderedPrompt").isEqualTo("Classify: login")
				.jsonPath("$.results[0].providerOutput").isEqualTo("Classify: login")
				.jsonPath("$.results[0].latencyMs").isEqualTo(3)
				.jsonPath("$.results[0].passed").isEqualTo(true);
	}

	@Test
	void retrievesAnEvaluationRun() {
		UUID runId = UUID.randomUUID();
		when(service.get(runId)).thenReturn(new EvaluationRunResponse(
				runId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				EvaluationRunStatus.COMPLETED,
				0.0,
				Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2026-01-01T00:00:01Z"),
				List.of()));

		client.get().uri("/api/evaluation-runs/{runId}", runId)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo(runId.toString())
				.jsonPath("$.status").isEqualTo("COMPLETED")
				.jsonPath("$.results").isArray();
	}

	@Test
	void rejectsMissingReferences() {
		client.post().uri("/api/evaluation-runs")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{}")
				.exchange()
				.expectStatus().isBadRequest();
	}
}
