package io.github.jitu2611.prompteval.datasets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "evaluation_cases", uniqueConstraints = @UniqueConstraint(columnNames = {"dataset_id", "case_number"}))
class EvaluationCase {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dataset_id", nullable = false, updatable = false)
	private EvaluationDataset dataset;

	@Column(name = "case_number", nullable = false, updatable = false)
	private int caseNumber;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "input_variables", nullable = false, columnDefinition = "json", updatable = false)
	private Map<String, String> inputVariables;

	@Column(name = "expected_output", nullable = false, columnDefinition = "TEXT", updatable = false)
	private String expectedOutput;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected EvaluationCase() {
	}

	EvaluationCase(EvaluationDataset dataset, int caseNumber, Map<String, String> inputVariables, String expectedOutput) {
		this.id = UUID.randomUUID();
		this.dataset = dataset;
		this.caseNumber = caseNumber;
		this.inputVariables = Map.copyOf(inputVariables);
		this.expectedOutput = expectedOutput;
		this.createdAt = Instant.now();
	}

	UUID getId() {
		return id;
	}

	int getCaseNumber() {
		return caseNumber;
	}

	Map<String, String> getInputVariables() {
		return Map.copyOf(inputVariables);
	}

	String getExpectedOutput() {
		return expectedOutput;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
