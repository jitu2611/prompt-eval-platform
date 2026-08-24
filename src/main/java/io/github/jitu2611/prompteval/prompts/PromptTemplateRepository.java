package io.github.jitu2611.prompteval.prompts;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {
}
