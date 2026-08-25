package io.github.jitu2611.prompteval.datasets;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

record EvaluationDatasetResponse(UUID id, String name, Instant createdAt, List<EvaluationCaseResponse> cases) {
}

record EvaluationCaseResponse(
		UUID id, int caseNumber, Map<String, String> inputVariables, String expectedOutput, Instant createdAt) {
}
