package io.github.jitu2611.prompteval.runs;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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
	void persistsReferencesAndPendingStatus() {
		UUID templateId = UUID.randomUUID();
		UUID promptVersionId = UUID.randomUUID();
		UUID datasetId = UUID.randomUUID();
		Timestamp now = Timestamp.from(Instant.now());
		jdbc.update("INSERT INTO prompt_templates (id, name, created_at) VALUES (?, ?, ?)", templateId, "support", now);
		jdbc.update("INSERT INTO prompt_template_versions (id, template_id, version_number, content, created_at) VALUES (?, ?, ?, ?, ?)",
				promptVersionId, templateId, 1, "Summarize {{ticket}}", now);
		jdbc.update("INSERT INTO evaluation_datasets (id, name, created_at) VALUES (?, ?, ?)", datasetId, "tickets", now);

		EvaluationRun run = repository.saveAndFlush(new EvaluationRun(promptVersionId, datasetId));
		entityManager.clear();

		EvaluationRun persisted = repository.findById(run.getId()).orElseThrow();
		assertThat(persisted.getPromptVersionId()).isEqualTo(promptVersionId);
		assertThat(persisted.getDatasetId()).isEqualTo(datasetId);
		assertThat(persisted.getStatus()).isEqualTo(EvaluationRunStatus.PENDING);
	}
}
