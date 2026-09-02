package io.github.jitu2611.prompteval.runs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
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

	protected EvaluationRun() {
	}

	EvaluationRun(UUID promptVersionId, UUID datasetId) {
		this.id = UUID.randomUUID();
		this.promptVersionId = promptVersionId;
		this.datasetId = datasetId;
		this.status = EvaluationRunStatus.PENDING;
		this.createdAt = Instant.now();
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
}

enum EvaluationRunStatus {
	PENDING
}
