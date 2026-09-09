package io.github.jitu2611.prompteval.runs.providers;

import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * A local provider that echoes the rendered prompt, making demo outputs stable across runs.
 */
@Component
public final class DeterministicLocalEvaluationProvider implements EvaluationProvider {

	@Override
	public String generate(String renderedPrompt) {
		return Objects.requireNonNull(renderedPrompt, "renderedPrompt must not be null");
	}
}
