package io.github.jitu2611.prompteval.prompts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaPromptVersionLookup.class)
class PromptVersionLookupRepositoryTest {

	@Autowired
	private PromptTemplateRepository repository;

	@Autowired
	private PromptVersionLookup lookup;

	@Test
	void findsOnlyPersistedPromptVersions() {
		PromptTemplate template = new PromptTemplate("support");
		PromptTemplateVersion version = template.addVersion("Summarize {{ticket}}");
		repository.saveAndFlush(template);

		assertThat(lookup.exists(version.getId())).isTrue();
		assertThat(lookup.exists(UUID.randomUUID())).isFalse();
	}
}
