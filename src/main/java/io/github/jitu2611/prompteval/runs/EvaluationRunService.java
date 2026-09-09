package io.github.jitu2611.prompteval.runs;

import io.github.jitu2611.prompteval.datasets.EvaluationDatasetLookup;
import io.github.jitu2611.prompteval.datasets.EvaluationDatasetLookup.Dataset;
import io.github.jitu2611.prompteval.prompts.PromptVersionLookup;
import io.github.jitu2611.prompteval.runs.providers.EvaluationProvider;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
class EvaluationRunService {

	private final EvaluationRunRepository repository;
	private final PromptVersionLookup promptVersions;
	private final EvaluationDatasetLookup datasets;
	private final PromptRenderer renderer;
	private final EvaluationProvider provider;

	EvaluationRunService(
			EvaluationRunRepository repository,
			PromptVersionLookup promptVersions,
			EvaluationDatasetLookup datasets,
			PromptRenderer renderer,
			EvaluationProvider provider) {
		this.repository = repository;
		this.promptVersions = promptVersions;
		this.datasets = datasets;
		this.renderer = renderer;
		this.provider = provider;
	}

	EvaluationRunResponse create(CreateEvaluationRunRequest request) {
		String promptContent = promptVersions.findContent(request.promptVersionId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt version not found"));
		Dataset dataset = datasets.find(request.datasetId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation dataset not found"));

		EvaluationRun run = new EvaluationRun(request.promptVersionId(), request.datasetId());
		for (EvaluationDatasetLookup.Case evaluationCase : dataset.cases()) {
			String renderedPrompt = renderer.render(promptContent, evaluationCase.inputVariables());
			long startedAt = System.nanoTime();
			String providerOutput = provider.generate(renderedPrompt);
			long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
			boolean passed = Objects.equals(providerOutput, evaluationCase.expectedOutput());
			run.addResult(
					evaluationCase.id(),
					evaluationCase.caseNumber(),
					renderedPrompt,
					providerOutput,
					latencyMs,
					passed);
		}
		run.complete();

		return toResponse(repository.save(run));
	}

	EvaluationRunResponse get(UUID runId) {
		return repository.findById(runId)
				.map(this::toResponse)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation run not found"));
	}

	private EvaluationRunResponse toResponse(EvaluationRun run) {
		return new EvaluationRunResponse(
				run.getId(),
				run.getPromptVersionId(),
				run.getDatasetId(),
				run.getStatus(),
				run.getPassRate(),
				run.getCreatedAt(),
				run.getCompletedAt(),
				run.getResults().stream()
						.map(result -> new EvaluationCaseResultResponse(
								result.getId(),
								result.getEvaluationCaseId(),
								result.getCaseNumber(),
								result.getRenderedPrompt(),
								result.getProviderOutput(),
								result.getLatencyMs(),
								result.isPassed()))
						.toList());
	}
}
