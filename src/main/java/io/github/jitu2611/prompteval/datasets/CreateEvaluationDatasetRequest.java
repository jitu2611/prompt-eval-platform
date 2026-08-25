package io.github.jitu2611.prompteval.datasets;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

record CreateEvaluationDatasetRequest(
		@NotBlank @Size(max = 120) String name,
		@NotEmpty List<@Valid CreateEvaluationCaseRequest> cases) {
}

record CreateEvaluationCaseRequest(
		@NotNull Map<@NotBlank @Size(max = 120) String, @NotNull @Size(max = 50_000) String> inputVariables,
		@NotBlank @Size(max = 50_000) String expectedOutput) {
}
