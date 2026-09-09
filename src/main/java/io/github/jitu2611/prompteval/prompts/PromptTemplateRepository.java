package io.github.jitu2611.prompteval.prompts;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {

	@Query("select version.content from PromptTemplate template join template.versions version where version.id = :versionId")
	Optional<String> findVersionContentById(@Param("versionId") UUID versionId);
}
