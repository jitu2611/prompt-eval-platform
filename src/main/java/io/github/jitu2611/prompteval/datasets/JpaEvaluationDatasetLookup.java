package io.github.jitu2611.prompteval.datasets;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaEvaluationDatasetLookup implements EvaluationDatasetLookup {

	private final EvaluationDatasetRepository repository;

	JpaEvaluationDatasetLookup(EvaluationDatasetRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean exists(UUID datasetId) {
		return repository.existsById(datasetId);
	}
}
