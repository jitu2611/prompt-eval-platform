package io.github.jitu2611.prompteval.prompts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PromptTemplateServiceTest {

	@Mock
	private PromptTemplateRepository repository;

	@InjectMocks
	private PromptTemplateService service;

	@Test
	void createsTemplateWithItsFirstImmutableVersion() {
		when(repository.save(any(PromptTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PromptTemplateResponse response = service.create(new CreatePromptTemplateRequest("summarize", "Summarize {{text}}"));

		assertThat(response.name()).isEqualTo("summarize");
		assertThat(response.versions()).singleElement().satisfies(version -> {
			assertThat(version.version()).isEqualTo(1);
			assertThat(version.content()).isEqualTo("Summarize {{text}}");
		});
	}

	@Test
	void addsTheNextVersionWithoutChangingTheExistingVersion() {
		PromptTemplate template = new PromptTemplate("summarize");
		template.addVersion("Summarize {{text}}");
		when(repository.findById(template.getId())).thenReturn(java.util.Optional.of(template));

		PromptTemplateResponse response = service.addVersion(template.getId(), new CreatePromptTemplateVersionRequest("Summarize briefly {{text}}"));

		assertThat(response.versions()).extracting(PromptTemplateVersionResponse::version).containsExactly(1, 2);
		assertThat(response.versions()).extracting(PromptTemplateVersionResponse::content)
				.containsExactly("Summarize {{text}}", "Summarize briefly {{text}}");
	}

	@Test
	void rejectsVersionsForUnknownTemplates() {
		UUID templateId = UUID.randomUUID();
		when(repository.findById(templateId)).thenReturn(java.util.Optional.empty());

		assertThatThrownBy(() -> service.addVersion(templateId, new CreatePromptTemplateVersionRequest("content")))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode().value()).isEqualTo(404));
	}
}
