package io.github.jitu2611.prompteval.datasets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
