package io.github.jitu2611.prompteval.runs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
	void createsAPendingEvaluationRun() {
		UUID runId = UUID.randomUUID();
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		EvaluationRunResponse response = new EvaluationRunResponse(
				runId, promptVersionId, datasetId, EvaluationRunStatus.PENDING, Instant.parse("2026-01-01T00:00:00Z"));
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
				.jsonPath("$.id").isEqualTo(runId.toString())
				.jsonPath("$.promptVersionId").isEqualTo(promptVersionId.toString())
				.jsonPath("$.datasetId").isEqualTo(datasetId.toString())
				.jsonPath("$.status").isEqualTo("PENDING");
	}

	@Test
	void retrievesAnEvaluationRun() {
		UUID runId = UUID.randomUUID();
		when(service.get(runId)).thenReturn(new EvaluationRunResponse(
				runId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				EvaluationRunStatus.PENDING,
				Instant.parse("2026-01-01T00:00:00Z")));

		client.get().uri("/api/evaluation-runs/{runId}", runId)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo(runId.toString())
				.jsonPath("$.status").isEqualTo("PENDING");
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
