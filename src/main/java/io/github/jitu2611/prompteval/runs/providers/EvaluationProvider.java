package io.github.jitu2611.prompteval.runs.providers;

/**
 * Generates an evaluation output from a fully rendered prompt.
 */
public interface EvaluationProvider {

	String generate(String renderedPrompt);
}
