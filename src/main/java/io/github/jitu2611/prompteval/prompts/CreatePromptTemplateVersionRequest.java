package io.github.jitu2611.prompteval.prompts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreatePromptTemplateVersionRequest(@NotBlank @Size(max = 50_000) String content) {
}
