package io.github.jitu2611.prompteval;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DashboardStaticResourceTest {

	@Autowired
	private WebTestClient client;

	@Test
	void servesAccessibleDashboardAtTheApplicationRoot() {
		String dashboard = client.get().uri("/")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();

		assertThat(dashboard)
				.contains("<main id=\"workspace\">")
				.contains("<form id=\"evaluation-form\"")
				.contains("aria-live=\"polite\"")
				.contains("role=\"alert\"")
				.contains("Run deterministic evaluation")
				.contains("pass rate")
				.contains("Case details");
	}

	@Test
	void servesDashboardAssetsThatDriveTheExistingApiWorkflow() {
		String script = client.get().uri("/app.js")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.valueOf("text/javascript"))
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();

		assertThat(script)
				.contains("/api/prompt-templates")
				.contains("/api/evaluation-datasets")
				.contains("/api/evaluation-runs")
				.contains("result.latencyMs")
				.contains("result.providerOutput");

		client.get().uri("/styles.css")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.valueOf("text/css"))
				.expectBody(String.class)
				.value(styles -> assertThat(styles).contains(":focus", "prefers-reduced-motion"));
	}
}
