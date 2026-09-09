package io.github.jitu2611.prompteval.runs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(
		name = "evaluation_case_results",
		uniqueConstraints = @UniqueConstraint(columnNames = {"run_id", "case_number"}))
class EvaluationCaseResult {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "run_id", nullable = false, updatable = false)
	private EvaluationRun run;

	@Column(name = "evaluation_case_id", nullable = false, updatable = false)
	private UUID evaluationCaseId;

	@Column(name = "case_number", nullable = false, updatable = false)
	private int caseNumber;

	@Column(name = "rendered_prompt", nullable = false, columnDefinition = "TEXT", updatable = false)
	private String renderedPrompt;

	@Column(name = "provider_output", nullable = false, columnDefinition = "TEXT", updatable = false)
	private String providerOutput;

	@Column(name = "latency_ms", nullable = false, updatable = false)
	private long latencyMs;

	@Column(nullable = false, updatable = false)
	private boolean passed;

	protected EvaluationCaseResult() {
	}

	EvaluationCaseResult(
			EvaluationRun run,
			UUID evaluationCaseId,
			int caseNumber,
			String renderedPrompt,
			String providerOutput,
			long latencyMs,
			boolean passed) {
		this.id = UUID.randomUUID();
		this.run = run;
		this.evaluationCaseId = evaluationCaseId;
		this.caseNumber = caseNumber;
		this.renderedPrompt = renderedPrompt;
		this.providerOutput = providerOutput;
		this.latencyMs = latencyMs;
		this.passed = passed;
	}

	UUID getId() {
		return id;
	}

	UUID getEvaluationCaseId() {
		return evaluationCaseId;
	}

	int getCaseNumber() {
		return caseNumber;
	}

	String getRenderedPrompt() {
		return renderedPrompt;
	}

	String getProviderOutput() {
		return providerOutput;
	}

	long getLatencyMs() {
		return latencyMs;
	}

	boolean isPassed() {
		return passed;
	}
}
