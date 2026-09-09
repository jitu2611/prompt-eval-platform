package io.github.jitu2611.prompteval.runs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
class EvaluationRunRepositoryTest {

	@Autowired
	private EvaluationRunRepository repository;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private EntityManager entityManager;

	@Test
	void persistsCompletedRunAndCaseResults() {
		UUID templateId = UUID.randomUUID();
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		UUID evaluationCaseId = UUID.randomUUID();
		Timestamp now = Timestamp.from(Instant.now());
		jdbc.update("INSERT INTO prompt_templates (id, name, created_at) VALUES (?, ?, ?)", templateId, "support", now);
		jdbc.update("INSERT INTO prompt_template_versions (id, template_id, version_number, content, created_at) VALUES (?, ?, ?, ?, ?)",
				promptVersionId, templateId, 1, "Summarize {{ticket}}", now);
		jdbc.update("INSERT INTO evaluation_datasets (id, name, created_at) VALUES (?, ?, ?)", datasetId, "tickets", now);
		jdbc.update("INSERT INTO evaluation_cases (id, dataset_id, case_number, input_variables, expected_output, created_at) VALUES (?, ?, ?, ?, ?, ?)",
				evaluationCaseId, datasetId, 1, "{\"ticket\":\"Cannot log in\"}", "Summarize Cannot log in", now);
		EvaluationRun run = new EvaluationRun(promptVersionId, datasetId);
		run.addResult(evaluationCaseId, 1, "Summarize Cannot log in", "Summarize Cannot log in", 4, true);
		run.complete();

		repository.saveAndFlush(run);
		entityManager.clear();

		EvaluationRun persisted = repository.findById(run.getId()).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(EvaluationRunStatus.COMPLETED);
		assertThat(persisted.getPassRate()).isEqualTo(1.0);
		assertThat(persisted.getCompletedAt()).isNotNull();
		assertThat(persisted.getResults()).singleElement().satisfies(result -> {
			assertThat(result.getEvaluationCaseId()).isEqualTo(evaluationCaseId);
			assertThat(result.getRenderedPrompt()).isEqualTo("Summarize Cannot log in");
			assertThat(result.getProviderOutput()).isEqualTo("Summarize Cannot log in");
			assertThat(result.getLatencyMs()).isEqualTo(4);
			assertThat(result.isPassed()).isTrue();
		});
	}

	@Test
	void rejectsRunsWithUnknownReferences() {
		EvaluationRun run = new EvaluationRun(UUID.randomUUID(), UUID.randomUUID());

		assertThatThrownBy(() -> repository.saveAndFlush(run))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
