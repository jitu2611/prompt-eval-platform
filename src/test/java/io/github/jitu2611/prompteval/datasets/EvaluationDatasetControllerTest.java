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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

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
	void getsADatasetWithCasesInCaseNumberOrder() {
		UUID datasetId = UUID.randomUUID();
		when(service.get(datasetId)).thenReturn(new EvaluationDatasetResponse(
				datasetId,
				"support tickets",
				Instant.parse("2026-01-01T00:00:00Z"),
				List.of(
						new EvaluationCaseResponse(
								UUID.randomUUID(),
								1,
								Map.of("ticket", "Cannot log in"),
								"Login issue",
								Instant.parse("2026-01-01T00:00:00Z")),
						new EvaluationCaseResponse(
								UUID.randomUUID(),
								2,
								Map.of("ticket", "Invoice is wrong"),
								"Billing issue",
								Instant.parse("2026-01-01T00:00:00Z")))));

		client.get().uri("/api/evaluation-datasets/{datasetId}", datasetId)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo(datasetId.toString())
				.jsonPath("$.name").isEqualTo("support tickets")
				.jsonPath("$.cases[0].caseNumber").isEqualTo(1)
				.jsonPath("$.cases[1].caseNumber").isEqualTo(2);
	}

	@Test
	void returnsNotFoundForAnUnknownDataset() {
		UUID datasetId = UUID.randomUUID();
		when(service.get(datasetId))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation dataset not found"));

		client.get().uri("/api/evaluation-datasets/{datasetId}", datasetId)
				.exchange()
				.expectStatus().isNotFound();
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
