package io.github.jitu2611.prompteval.datasets;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class EvaluationDatasetRepositoryTest {

	@Autowired
	private EvaluationDatasetRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void persistsCasesWithInputVariablesAndExpectedOutputs() {
		EvaluationDataset dataset = new EvaluationDataset("support tickets");
		dataset.addCase(new CreateEvaluationCaseRequest(Map.of("ticket", "Cannot log in"), "Login issue"));
		dataset.addCase(new CreateEvaluationCaseRequest(Map.of("ticket", "Invoice is wrong"), "Billing issue"));
		repository.saveAndFlush(dataset);
		entityManager.clear();

		EvaluationDataset persisted = repository.findById(dataset.getId()).orElseThrow();

		assertThat(persisted.getName()).isEqualTo("support tickets");
		assertThat(persisted.getCases()).extracting(EvaluationCase::getCaseNumber).containsExactly(1, 2);
		assertThat(persisted.getCases()).extracting(EvaluationCase::getInputVariables)
				.containsExactly(Map.of("ticket", "Cannot log in"), Map.of("ticket", "Invoice is wrong"));
		assertThat(persisted.getCases()).extracting(EvaluationCase::getExpectedOutput)
				.containsExactly("Login issue", "Billing issue");
	}
}
