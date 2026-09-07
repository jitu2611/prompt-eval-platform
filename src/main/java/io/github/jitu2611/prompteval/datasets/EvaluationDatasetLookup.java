package io.github.jitu2611.prompteval.datasets;

import java.util.UUID;

public interface EvaluationDatasetLookup {

	boolean exists(UUID datasetId);
}
