package io.github.jitu2611.prompteval.runs;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evaluation_runs")
class EvaluationRun {

	@Id
	private UUID id;

	@Column(nullable = false, updatable = false)
	private UUID promptVersionId;

	@Column(nullable = false, updatable = false)
	private UUID datasetId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EvaluationRunStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Double passRate;

	private Instant completedAt;

	@OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("caseNumber ASC")
	private List<EvaluationCaseResult> results = new ArrayList<>();

	protected EvaluationRun() {
	}

	EvaluationRun(UUID promptVersionId, UUID datasetId) {
		this.id = UUID.randomUUID();
		this.promptVersionId = promptVersionId;
		this.datasetId = datasetId;
		this.status = EvaluationRunStatus.PENDING;
		this.createdAt = Instant.now();
	}

	void addResult(
			UUID evaluationCaseId,
			int caseNumber,
			String renderedPrompt,
			String providerOutput,
			long latencyMs,
			boolean passed) {
		results.add(new EvaluationCaseResult(
				this, evaluationCaseId, caseNumber, renderedPrompt, providerOutput, latencyMs, passed));
	}

	void complete() {
		long passedCases = results.stream().filter(EvaluationCaseResult::isPassed).count();
		this.passRate = (double) passedCases / results.size();
		this.status = EvaluationRunStatus.COMPLETED;
		this.completedAt = Instant.now();
	}

	UUID getId() {
		return id;
	}

	UUID getPromptVersionId() {
		return promptVersionId;
	}

	UUID getDatasetId() {
		return datasetId;
	}

	EvaluationRunStatus getStatus() {
		return status;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Double getPassRate() {
		return passRate;
	}

	Instant getCompletedAt() {
		return completedAt;
	}

	List<EvaluationCaseResult> getResults() {
		return List.copyOf(results);
	}
}

enum EvaluationRunStatus {
	PENDING,
	COMPLETED
}
