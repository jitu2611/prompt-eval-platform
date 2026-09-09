package io.github.jitu2611.prompteval.prompts;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaPromptVersionLookup implements PromptVersionLookup {

	private final PromptTemplateRepository repository;

	JpaPromptVersionLookup(PromptTemplateRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<String> findContent(UUID promptVersionId) {
		return repository.findVersionContentById(promptVersionId);
	}
}
