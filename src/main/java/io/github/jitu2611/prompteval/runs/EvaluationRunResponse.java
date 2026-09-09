package io.github.jitu2611.prompteval.runs;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record EvaluationRunResponse(
		UUID id,
		UUID promptVersionId,
		UUID datasetId,
		EvaluationRunStatus status,
		Double passRate,
		Instant createdAt,
		Instant completedAt,
		List<EvaluationCaseResultResponse> results) {

	EvaluationRunResponse {
		results = List.copyOf(results);
	}
}

record EvaluationCaseResultResponse(
		UUID id,
		UUID evaluationCaseId,
		int caseNumber,
		String renderedPrompt,
		String providerOutput,
		long latencyMs,
		boolean passed) {
}
