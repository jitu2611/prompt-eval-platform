package io.github.jitu2611.prompteval.runs;

import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Renders double-braced dataset variables with a single, literal substitution pass.
 */
@Component
final class PromptRenderer {

	String render(String promptContent, Map<String, String> variables) {
		Objects.requireNonNull(promptContent, "promptContent must not be null");
		Objects.requireNonNull(variables, "variables must not be null");

		StringBuilder rendered = new StringBuilder(promptContent.length());
		int cursor = 0;
		while (cursor < promptContent.length()) {
			int opening = promptContent.indexOf("{{", cursor);
			int unexpectedClosing = promptContent.indexOf("}}", cursor);
			if (unexpectedClosing >= 0 && (opening < 0 || unexpectedClosing < opening)) {
				throw new PromptRenderingException("Unexpected closing delimiter at position " + unexpectedClosing);
			}
			if (opening < 0) {
				rendered.append(promptContent, cursor, promptContent.length());
				break;
			}

			rendered.append(promptContent, cursor, opening);
			int closing = promptContent.indexOf("}}", opening + 2);
			if (closing < 0) {
				throw new PromptRenderingException("Unclosed placeholder at position " + opening);
			}
			int nestedOpening = promptContent.indexOf("{{", opening + 2);
			if (nestedOpening >= 0 && nestedOpening < closing) {
				throw new PromptRenderingException("Nested placeholder at position " + nestedOpening);
			}

			String variableName = promptContent.substring(opening + 2, closing).strip();
			if (variableName.isEmpty()) {
				throw new PromptRenderingException("Placeholder name must not be blank at position " + opening);
			}
			if (!variables.containsKey(variableName) || variables.get(variableName) == null) {
				throw new PromptRenderingException("No dataset variable supplied for placeholder '" + variableName + "'");
			}

			// Append the value directly so replacement syntax and placeholder-like content stay literal.
			rendered.append(variables.get(variableName));
			cursor = closing + 2;
		}
		return rendered.toString();
	}
}
