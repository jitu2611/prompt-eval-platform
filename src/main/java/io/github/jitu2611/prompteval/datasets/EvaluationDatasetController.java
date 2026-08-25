package io.github.jitu2611.prompteval.datasets;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluation-datasets")
class EvaluationDatasetController {

	private final EvaluationDatasetService service;

	EvaluationDatasetController(EvaluationDatasetService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<EvaluationDatasetResponse> create(@Valid @RequestBody CreateEvaluationDatasetRequest request) {
		EvaluationDatasetResponse response = service.create(request);
		return ResponseEntity.created(URI.create("/api/evaluation-datasets/" + response.id())).body(response);
	}
}
