package io.github.jitu2611.prompteval.runs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.jitu2611.prompteval.datasets.EvaluationDatasetLookup;
import io.github.jitu2611.prompteval.datasets.EvaluationDatasetLookup.Case;
import io.github.jitu2611.prompteval.datasets.EvaluationDatasetLookup.Dataset;
import io.github.jitu2611.prompteval.prompts.PromptVersionLookup;
import io.github.jitu2611.prompteval.runs.providers.EvaluationProvider;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EvaluationRunServiceTest {

	@Mock
	private EvaluationRunRepository repository;

	@Mock
	private PromptVersionLookup promptVersions;

	@Mock
	private EvaluationDatasetLookup datasets;

	@Mock
	private EvaluationProvider provider;

	private EvaluationRunService service;

	@BeforeEach
	void setUp() {
		service = new EvaluationRunService(repository, promptVersions, datasets, new PromptRenderer(), provider);
	}

	@Test
	void executesCasesAndCompletesRunWithExactMatchPassRate() {
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		UUID passingCaseId = UUID.randomUUID();
		UUID failingCaseId = UUID.randomUUID();
		when(promptVersions.findContent(promptVersionId)).thenReturn(Optional.of("Classify: {{ticket}}"));
		when(datasets.find(datasetId)).thenReturn(Optional.of(new Dataset(List.of(
				new Case(passingCaseId, 1, Map.of("ticket", "login"), "Classify: login"),
				new Case(failingCaseId, 2, Map.of("ticket", "invoice"), "Billing")))));
		when(provider.generate(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(repository.save(any(EvaluationRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EvaluationRunResponse response = service.create(new CreateEvaluationRunRequest(promptVersionId, datasetId));

		assertThat(response.status()).isEqualTo(EvaluationRunStatus.COMPLETED);
		assertThat(response.passRate()).isEqualTo(0.5);
		assertThat(response.completedAt()).isNotNull();
		assertThat(response.results()).hasSize(2);
		assertThat(response.results().get(0))
				.extracting(
						EvaluationCaseResultResponse::evaluationCaseId,
						EvaluationCaseResultResponse::renderedPrompt,
						EvaluationCaseResultResponse::providerOutput,
						EvaluationCaseResultResponse::passed)
				.containsExactly(passingCaseId, "Classify: login", "Classify: login", true);
		assertThat(response.results().get(1).passed()).isFalse();
		assertThat(response.results()).allSatisfy(result -> assertThat(result.latencyMs()).isNotNegative());
		verify(provider).generate("Classify: login");
		verify(provider).generate("Classify: invoice");
	}

	@Test
	void rejectsAnUnknownPromptVersion() {
		UUID promptVersionId = UUID.randomUUID();
		when(promptVersions.findContent(promptVersionId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.create(new CreateEvaluationRunRequest(promptVersionId, UUID.randomUUID())))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void rejectsAnUnknownDataset() {
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		when(promptVersions.findContent(promptVersionId)).thenReturn(Optional.of("prompt"));
		when(datasets.find(datasetId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.create(new CreateEvaluationRunRequest(promptVersionId, datasetId)))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void retrievesAnExistingRunWithResults() {
		EvaluationRun run = new EvaluationRun(UUID.randomUUID(), UUID.randomUUID());
		run.addResult(UUID.randomUUID(), 1, "rendered", "rendered", 3, true);
		run.complete();
		when(repository.findById(run.getId())).thenReturn(Optional.of(run));

		EvaluationRunResponse response = service.get(run.getId());

		assertThat(response.id()).isEqualTo(run.getId());
		assertThat(response.results()).singleElement().satisfies(result -> {
			assertThat(result.renderedPrompt()).isEqualTo("rendered");
			assertThat(result.providerOutput()).isEqualTo("rendered");
			assertThat(result.latencyMs()).isEqualTo(3);
			assertThat(result.passed()).isTrue();
		});
	}

	@Test
	void rejectsAnUnknownRun() {
		UUID runId = UUID.randomUUID();
		when(repository.findById(runId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get(runId))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}
}
