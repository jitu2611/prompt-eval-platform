package io.github.jitu2611.prompteval.datasets;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaEvaluationDatasetLookup implements EvaluationDatasetLookup {

	private final EvaluationDatasetRepository repository;

	JpaEvaluationDatasetLookup(EvaluationDatasetRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Dataset> find(UUID datasetId) {
		return repository.findWithCasesById(datasetId)
				.map(dataset -> new Dataset(dataset.getCases().stream()
						.map(evaluationCase -> new Case(
								evaluationCase.getId(),
								evaluationCase.getCaseNumber(),
								evaluationCase.getInputVariables(),
								evaluationCase.getExpectedOutput()))
						.toList()));
	}
}
