package io.github.jitu2611.prompteval;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class EvaluationWorkflowApiEndToEndTest {

	@Autowired
	private WebTestClient client;

	@Test
	void createsAndExecutesACompleteEvaluationWorkflowThroughTheApi() {
		TemplateResponse template = client.post().uri("/api/prompt-templates")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"name":"ticket-classifier","content":"Legacy classification: {{ticket}}"}
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().exists("Location")
				.expectBody(TemplateResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(template).isNotNull();
		assertThat(template.versions()).singleElement()
				.satisfies(version -> assertThat(version.version()).isEqualTo(1));

		TemplateResponse versionedTemplate = client.post()
				.uri("/api/prompt-templates/{templateId}/versions", template.id())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"content":"Classify: {{ticket}}"}
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectBody(TemplateResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(versionedTemplate).isNotNull();
		assertThat(versionedTemplate.versions()).extracting(VersionResponse::version).containsExactly(1, 2);
		VersionResponse promptVersion = versionedTemplate.versions().get(1);

		DatasetResponse dataset = client.post().uri("/api/evaluation-datasets")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  "name":"ticket-cases",
						  "cases":[
						    {"inputVariables":{"ticket":"Cannot log in"},"expectedOutput":"Classify: Cannot log in"},
						    {"inputVariables":{"ticket":"Invoice is wrong"},"expectedOutput":"Billing issue"}
						  ]
						}
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().exists("Location")
				.expectBody(DatasetResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(dataset).isNotNull();
		assertThat(dataset.cases()).extracting(CaseResponse::caseNumber).containsExactly(1, 2);

		RunResponse createdRun = client.post().uri("/api/evaluation-runs")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"promptVersionId":"%s","datasetId":"%s"}
						""".formatted(promptVersion.id(), dataset.id()))
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().exists("Location")
				.expectBody(RunResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(createdRun).isNotNull();
		assertThat(createdRun.promptVersionId()).isEqualTo(promptVersion.id());
		assertThat(createdRun.datasetId()).isEqualTo(dataset.id());
		assertThat(createdRun.status()).isEqualTo("COMPLETED");
		assertThat(createdRun.passRate()).isEqualTo(0.5);
		assertThat(createdRun.completedAt()).isNotNull();
		assertThat(createdRun.results()).extracting(ResultResponse::caseNumber).containsExactly(1, 2);
		assertThat(createdRun.results()).extracting(ResultResponse::renderedPrompt)
				.containsExactly("Classify: Cannot log in", "Classify: Invoice is wrong");
		assertThat(createdRun.results()).extracting(ResultResponse::providerOutput)
				.containsExactly("Classify: Cannot log in", "Classify: Invoice is wrong");
		assertThat(createdRun.results()).extracting(ResultResponse::passed).containsExactly(true, false);
		assertThat(createdRun.results()).allSatisfy(result -> assertThat(result.latencyMs()).isNotNegative());

		RunResponse retrievedRun = client.get().uri("/api/evaluation-runs/{runId}", createdRun.id())
				.exchange()
				.expectStatus().isOk()
				.expectBody(RunResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(retrievedRun).usingRecursiveComparison().isEqualTo(createdRun);
	}

	private record TemplateResponse(UUID id, String name, Instant createdAt, List<VersionResponse> versions) {
	}

	private record VersionResponse(UUID id, int version, String content, Instant createdAt) {
	}

	private record DatasetResponse(UUID id, String name, Instant createdAt, List<CaseResponse> cases) {
	}

	private record CaseResponse(
			UUID id, int caseNumber, Map<String, String> inputVariables, String expectedOutput, Instant createdAt) {
	}

	private record RunResponse(
			UUID id,
			UUID promptVersionId,
			UUID datasetId,
			String status,
			Double passRate,
			Instant createdAt,
			Instant completedAt,
			List<ResultResponse> results) {
	}

	private record ResultResponse(
			UUID id,
			UUID evaluationCaseId,
			int caseNumber,
			String renderedPrompt,
			String providerOutput,
			long latencyMs,
			boolean passed) {
	}
}
