package io.github.jitu2611.prompteval.runs;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record CreateEvaluationRunRequest(
		@NotNull UUID promptVersionId,
		@NotNull UUID datasetId) {
}
