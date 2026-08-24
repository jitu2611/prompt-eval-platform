package io.github.jitu2611.prompteval.prompts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreatePromptTemplateRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 50_000) String content) {
}
