package io.github.jitu2611.prompteval.datasets;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
class EvaluationDatasetService {

	private final EvaluationDatasetRepository repository;

	EvaluationDatasetService(EvaluationDatasetRepository repository) {
		this.repository = repository;
	}

	EvaluationDatasetResponse create(CreateEvaluationDatasetRequest request) {
		EvaluationDataset dataset = new EvaluationDataset(request.name());
		request.cases().forEach(dataset::addCase);
		return toResponse(repository.save(dataset));
	}

	EvaluationDatasetResponse get(UUID datasetId) {
		EvaluationDataset dataset = repository.findById(datasetId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation dataset not found"));
		return toResponse(dataset);
	}

	private EvaluationDatasetResponse toResponse(EvaluationDataset dataset) {
		return new EvaluationDatasetResponse(
				dataset.getId(),
				dataset.getName(),
				dataset.getCreatedAt(),
				dataset.getCases().stream()
						.map(evaluationCase -> new EvaluationCaseResponse(
								evaluationCase.getId(),
								evaluationCase.getCaseNumber(),
								evaluationCase.getInputVariables(),
								evaluationCase.getExpectedOutput(),
								evaluationCase.getCreatedAt()))
						.toList());
	}
}
