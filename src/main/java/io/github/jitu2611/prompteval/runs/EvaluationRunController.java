package io.github.jitu2611.prompteval.runs;

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
@RequestMapping("/api/evaluation-runs")
class EvaluationRunController {

	private final EvaluationRunService service;

	EvaluationRunController(EvaluationRunService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<EvaluationRunResponse> create(@Valid @RequestBody CreateEvaluationRunRequest request) {
		EvaluationRunResponse response = service.create(request);
		return ResponseEntity.created(URI.create("/api/evaluation-runs/" + response.id())).body(response);
	}

	@GetMapping("/{runId}")
	EvaluationRunResponse get(@PathVariable UUID runId) {
		return service.get(runId);
	}
}
