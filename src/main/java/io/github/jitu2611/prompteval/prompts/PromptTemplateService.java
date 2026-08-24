package io.github.jitu2611.prompteval.prompts;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
class PromptTemplateService {

	private final PromptTemplateRepository repository;

	PromptTemplateService(PromptTemplateRepository repository) {
		this.repository = repository;
	}

	PromptTemplateResponse create(CreatePromptTemplateRequest request) {
		PromptTemplate template = new PromptTemplate(request.name());
		template.addVersion(request.content());
		return toResponse(repository.save(template));
	}

	PromptTemplateResponse get(UUID templateId) {
		return toResponse(findTemplate(templateId));
	}

	PromptTemplateResponse addVersion(UUID templateId, CreatePromptTemplateVersionRequest request) {
		PromptTemplate template = findTemplate(templateId);
		template.addVersion(request.content());
		return toResponse(template);
	}

	private PromptTemplate findTemplate(UUID templateId) {
		return repository.findById(templateId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt template not found"));
	}

	private PromptTemplateResponse toResponse(PromptTemplate template) {
		return new PromptTemplateResponse(
				template.getId(),
				template.getName(),
				template.getCreatedAt(),
				template.getVersions().stream()
						.map(version -> new PromptTemplateVersionResponse(
								version.getId(), version.getVersionNumber(), version.getContent(), version.getCreatedAt()))
						.toList());
	}
}
