package io.github.jitu2611.prompteval.datasets;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evaluation_datasets")
class EvaluationDataset {

	@Id
	private UUID id;

	@Column(nullable = false, length = 120, updatable = false)
	private String name;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("caseNumber ASC")
	private List<EvaluationCase> cases = new ArrayList<>();

	protected EvaluationDataset() {
	}

	EvaluationDataset(String name) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.createdAt = Instant.now();
	}

	void addCase(CreateEvaluationCaseRequest request) {
		cases.add(new EvaluationCase(this, cases.size() + 1, request.inputVariables(), request.expectedOutput()));
	}

	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	List<EvaluationCase> getCases() {
		return List.copyOf(cases);
	}
}
