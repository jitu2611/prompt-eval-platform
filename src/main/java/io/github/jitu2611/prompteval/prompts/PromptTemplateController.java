package io.github.jitu2611.prompteval.prompts;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prompt-templates")
class PromptTemplateController {

	private final PromptTemplateService service;

	PromptTemplateController(PromptTemplateService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<PromptTemplateResponse> create(@Valid @RequestBody CreatePromptTemplateRequest request) {
		PromptTemplateResponse response = service.create(request);
		return ResponseEntity.created(URI.create("/api/prompt-templates/" + response.id())).body(response);
	}

	@GetMapping("/{templateId}")
	PromptTemplateResponse get(@PathVariable UUID templateId) {
		return service.get(templateId);
	}

	@PostMapping("/{templateId}/versions")
	ResponseEntity<PromptTemplateResponse> addVersion(
			@PathVariable UUID templateId, @Valid @RequestBody CreatePromptTemplateVersionRequest request) {
		return ResponseEntity.status(201).body(service.addVersion(templateId, request));
	}
}
