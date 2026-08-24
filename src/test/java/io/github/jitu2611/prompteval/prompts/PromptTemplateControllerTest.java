package io.github.jitu2611.prompteval.prompts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(PromptTemplateController.class)
class PromptTemplateControllerTest {

	@Autowired
	private WebTestClient client;

	@MockitoBean
	private PromptTemplateService service;

	@Test
	void createsATemplate() {
		UUID templateId = UUID.randomUUID();
		PromptTemplateResponse response = new PromptTemplateResponse(
				templateId, "summarize", Instant.parse("2026-01-01T00:00:00Z"),
				List.of(new PromptTemplateVersionResponse(
						UUID.randomUUID(), 1, "Summarize {{text}}", Instant.parse("2026-01-01T00:00:00Z"))));
		when(service.create(any())).thenReturn(response);

		client.post().uri("/api/prompt-templates")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"name":"summarize","content":"Summarize {{text}}"}
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().valueEquals("Location", "/api/prompt-templates/" + templateId)
				.expectBody()
				.jsonPath("$.name").isEqualTo("summarize")
				.jsonPath("$.versions[0].version").isEqualTo(1)
				.jsonPath("$.versions[0].content").isEqualTo("Summarize {{text}}");
	}

	@Test
	void rejectsBlankTemplateContent() {
		client.post().uri("/api/prompt-templates")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"name":"summarize","content":" "}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void addsAVersion() {
		UUID templateId = UUID.randomUUID();
		when(service.addVersion(eq(templateId), any())).thenReturn(new PromptTemplateResponse(
				templateId, "summarize", Instant.parse("2026-01-01T00:00:00Z"), List.of()));

		client.post().uri("/api/prompt-templates/{templateId}/versions", templateId)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{" + "\"content\":\"Summarize briefly {{text}}\"}")
				.exchange()
				.expectStatus().isCreated();
	}
}
