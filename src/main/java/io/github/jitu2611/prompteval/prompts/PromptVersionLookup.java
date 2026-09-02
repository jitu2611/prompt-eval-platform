package io.github.jitu2611.prompteval.prompts;

import java.util.UUID;

public interface PromptVersionLookup {

	boolean exists(UUID promptVersionId);
}
