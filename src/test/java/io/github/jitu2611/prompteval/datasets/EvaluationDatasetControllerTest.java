package io.github.jitu2611.prompteval.datasets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(EvaluationDatasetController.class)
class EvaluationDatasetControllerTest {

	@Autowired
	private WebTestClient client;

	@MockitoBean
	private EvaluationDatasetService service;

	@Test
	void createsADatasetWithCases() {
		UUID datasetId = UUID.randomUUID();
		when(service.create(any())).thenReturn(new EvaluationDatasetResponse(
				datasetId,
				"support tickets",
				Instant.parse("2026-01-01T00:00:00Z"),
				List.of(new EvaluationCaseResponse(
						UUID.randomUUID(),
						1,
						Map.of("ticket", "Cannot log in"),
						"Login issue",
						Instant.parse("2026-01-01T00:00:00Z")))));

		client.post().uri("/api/evaluation-datasets")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"name":"support tickets","cases":[{"inputVariables":{"ticket":"Cannot log in"},"expectedOutput":"Login issue"}]}
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().valueEquals("Location", "/api/evaluation-datasets/" + datasetId)
				.expectBody()
				.jsonPath("$.name").isEqualTo("support tickets")
				.jsonPath("$.cases[0].caseNumber").isEqualTo(1)
				.jsonPath("$.cases[0].inputVariables.ticket").isEqualTo("Cannot log in")
				.jsonPath("$.cases[0].expectedOutput").isEqualTo("Login issue");
	}

	@Test
	void rejectsADatasetWithoutCases() {
		client.post().uri("/api/evaluation-datasets")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"name":"support tickets","cases":[]}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void rejectsBlankExpectedOutput() {
		client.post().uri("/api/evaluation-datasets")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"name":"support tickets","cases":[{"inputVariables":{},"expectedOutput":" "}]}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}
}
