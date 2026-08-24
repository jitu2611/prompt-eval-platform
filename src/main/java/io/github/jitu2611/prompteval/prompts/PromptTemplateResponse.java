package io.github.jitu2611.prompteval.prompts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record PromptTemplateResponse(UUID id, String name, Instant createdAt, List<PromptTemplateVersionResponse> versions) {
}

record PromptTemplateVersionResponse(UUID id, int version, String content, Instant createdAt) {
}
