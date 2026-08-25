package io.github.jitu2611.prompteval.datasets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationDatasetServiceTest {

	@Mock
	private EvaluationDatasetRepository repository;

	@InjectMocks
	private EvaluationDatasetService service;

	@Test
	void createsADatasetWithOrderedCasesAndExpectedOutputs() {
		when(repository.save(any(EvaluationDataset.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EvaluationDatasetResponse response = service.create(new CreateEvaluationDatasetRequest("support tickets", List.of(
				new CreateEvaluationCaseRequest(Map.of("ticket", "Cannot log in"), "Login issue"),
				new CreateEvaluationCaseRequest(Map.of("ticket", "Invoice is wrong"), "Billing issue"))));

		assertThat(response.name()).isEqualTo("support tickets");
		assertThat(response.cases()).extracting(EvaluationCaseResponse::caseNumber).containsExactly(1, 2);
		assertThat(response.cases()).extracting(EvaluationCaseResponse::inputVariables)
				.containsExactly(Map.of("ticket", "Cannot log in"), Map.of("ticket", "Invoice is wrong"));
		assertThat(response.cases()).extracting(EvaluationCaseResponse::expectedOutput)
				.containsExactly("Login issue", "Billing issue");
	}
}
