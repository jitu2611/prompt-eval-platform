package io.github.jitu2611.prompteval.datasets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface EvaluationDatasetLookup {

	Optional<Dataset> find(UUID datasetId);

	record Dataset(List<Case> cases) {

		public Dataset {
			cases = List.copyOf(cases);
		}
	}

	record Case(UUID id, int caseNumber, Map<String, String> inputVariables, String expectedOutput) {

		public Case {
			inputVariables = Map.copyOf(inputVariables);
		}
	}
}
