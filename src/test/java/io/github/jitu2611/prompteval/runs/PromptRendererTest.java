package io.github.jitu2611.prompteval.runs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptRendererTest {

	private final PromptRenderer renderer = new PromptRenderer();

	@Test
	void rendersNamedVariablesInPromptOrder() {
		String rendered = renderer.render(
				"Classify {{ ticket }} for {{customer}}. Repeat: {{ticket}}",
				Map.of("customer", "Ada", "ticket", "Cannot log in", "unused", "ignored"));

		assertThat(rendered).isEqualTo("Classify Cannot log in for Ada. Repeat: Cannot log in");
	}

	@Test
	void treatsVariableValuesAsLiteralContentInASinglePass() {
		String rendered = renderer.render(
				"Payload: {{payload}}; owner: {{owner}}",
				Map.of("payload", "$1 \\ {{owner}} }}", "owner", "Ada"));

		assertThat(rendered).isEqualTo("Payload: $1 \\ {{owner}} }}; owner: Ada");
	}

	@Test
	void rejectsMissingVariablesInsteadOfLeavingUnresolvedPlaceholders() {
		assertThatThrownBy(() -> renderer.render("Summarize {{ticket}}", Map.of()))
				.isInstanceOf(PromptRenderingException.class)
				.hasMessage("No dataset variable supplied for placeholder 'ticket'");
	}

	@Test
	void rejectsMalformedPlaceholders() {
		assertThatThrownBy(() -> renderer.render("Summarize {{ticket", Map.of("ticket", "text")))
				.isInstanceOf(PromptRenderingException.class)
				.hasMessageContaining("Unclosed placeholder");
		assertThatThrownBy(() -> renderer.render("Summarize ticket}}", Map.of("ticket", "text")))
				.isInstanceOf(PromptRenderingException.class)
				.hasMessageContaining("Unexpected closing delimiter");
		assertThatThrownBy(() -> renderer.render("Summarize {{  }}", Map.of()))
				.isInstanceOf(PromptRenderingException.class)
				.hasMessageContaining("Placeholder name must not be blank");
		assertThatThrownBy(() -> renderer.render("Summarize {{outer {{inner}}", Map.of()))
				.isInstanceOf(PromptRenderingException.class)
				.hasMessageContaining("Nested placeholder");
	}
}
