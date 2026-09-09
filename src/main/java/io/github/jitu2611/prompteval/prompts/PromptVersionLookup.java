package io.github.jitu2611.prompteval.prompts;

import java.util.Optional;
import java.util.UUID;

public interface PromptVersionLookup {

	Optional<String> findContent(UUID promptVersionId);
}
