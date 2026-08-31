package io.github.jitu2611.prompteval.datasets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

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

	@Test
	void getsADatasetWithCasesInCaseNumberOrder() {
		EvaluationDataset dataset = new EvaluationDataset("support tickets");
		dataset.addCase(new CreateEvaluationCaseRequest(Map.of("ticket", "Cannot log in"), "Login issue"));
		dataset.addCase(new CreateEvaluationCaseRequest(Map.of("ticket", "Invoice is wrong"), "Billing issue"));
		when(repository.findById(dataset.getId())).thenReturn(Optional.of(dataset));

		EvaluationDatasetResponse response = service.get(dataset.getId());

		assertThat(response.id()).isEqualTo(dataset.getId());
		assertThat(response.name()).isEqualTo("support tickets");
		assertThat(response.cases()).extracting(EvaluationCaseResponse::caseNumber).containsExactly(1, 2);
		assertThat(response.cases()).extracting(EvaluationCaseResponse::expectedOutput)
				.containsExactly("Login issue", "Billing issue");
	}

	@Test
	void rejectsUnknownDatasetsWhenGettingThem() {
		UUID datasetId = UUID.randomUUID();
		when(repository.findById(datasetId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get(datasetId))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode().value()).isEqualTo(404));
	}
}
