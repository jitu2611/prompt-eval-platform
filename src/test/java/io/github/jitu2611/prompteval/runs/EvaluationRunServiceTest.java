package io.github.jitu2611.prompteval.runs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.jitu2611.prompteval.datasets.EvaluationDatasetLookup;
import io.github.jitu2611.prompteval.prompts.PromptVersionLookup;
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

	private EvaluationRunService service;

	@BeforeEach
	void setUp() {
		service = new EvaluationRunService(repository, promptVersions, datasets);
	}

	@Test
	void createsAPendingRunForExistingInputs() {
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		when(promptVersions.exists(promptVersionId)).thenReturn(true);
		when(datasets.exists(datasetId)).thenReturn(true);
		when(repository.save(any(EvaluationRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EvaluationRunResponse response = service.create(new CreateEvaluationRunRequest(promptVersionId, datasetId));

		assertThat(response.id()).isNotNull();
		assertThat(response.promptVersionId()).isEqualTo(promptVersionId);
		assertThat(response.datasetId()).isEqualTo(datasetId);
		assertThat(response.status()).isEqualTo(EvaluationRunStatus.PENDING);
		assertThat(response.createdAt()).isNotNull();
		verify(repository).save(any(EvaluationRun.class));
	}

	@Test
	void rejectsAnUnknownPromptVersion() {
		UUID promptVersionId = UUID.randomUUID();
		when(promptVersions.exists(promptVersionId)).thenReturn(false);

		assertThatThrownBy(() -> service.create(new CreateEvaluationRunRequest(promptVersionId, UUID.randomUUID())))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void rejectsAnUnknownDataset() {
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		when(promptVersions.exists(promptVersionId)).thenReturn(true);
		when(datasets.exists(datasetId)).thenReturn(false);

		assertThatThrownBy(() -> service.create(new CreateEvaluationRunRequest(promptVersionId, datasetId)))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void retrievesAnExistingRun() {
		EvaluationRun run = new EvaluationRun(UUID.randomUUID(), UUID.randomUUID());
		when(repository.findById(run.getId())).thenReturn(Optional.of(run));

		assertThat(service.get(run.getId()).id()).isEqualTo(run.getId());
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
