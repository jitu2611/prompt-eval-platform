package io.github.jitu2611.prompteval.runs;

import io.github.jitu2611.prompteval.datasets.EvaluationDatasetLookup;
import io.github.jitu2611.prompteval.prompts.PromptVersionLookup;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
class EvaluationRunService {

	private final EvaluationRunRepository repository;
	private final PromptVersionLookup promptVersions;
	private final EvaluationDatasetLookup datasets;

	EvaluationRunService(
			EvaluationRunRepository repository,
			PromptVersionLookup promptVersions,
			EvaluationDatasetLookup datasets) {
		this.repository = repository;
		this.promptVersions = promptVersions;
		this.datasets = datasets;
	}

	EvaluationRunResponse create(CreateEvaluationRunRequest request) {
		if (!promptVersions.exists(request.promptVersionId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt version not found");
		}
		if (!datasets.exists(request.datasetId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation dataset not found");
		}

		return toResponse(repository.save(new EvaluationRun(request.promptVersionId(), request.datasetId())));
	}

	EvaluationRunResponse get(UUID runId) {
		return repository.findById(runId)
				.map(this::toResponse)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation run not found"));
	}

	private EvaluationRunResponse toResponse(EvaluationRun run) {
		return new EvaluationRunResponse(
				run.getId(), run.getPromptVersionId(), run.getDatasetId(), run.getStatus(), run.getCreatedAt());
	}
}
