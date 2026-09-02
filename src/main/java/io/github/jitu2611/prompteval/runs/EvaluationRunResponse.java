package io.github.jitu2611.prompteval.runs;

import java.time.Instant;
import java.util.UUID;

record EvaluationRunResponse(
		UUID id,
		UUID promptVersionId,
		UUID datasetId,
		EvaluationRunStatus status,
		Instant createdAt) {
}
