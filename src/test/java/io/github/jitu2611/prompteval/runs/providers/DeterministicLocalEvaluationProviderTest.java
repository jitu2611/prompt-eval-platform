package io.github.jitu2611.prompteval.runs.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class DeterministicLocalEvaluationProviderTest {

	private final EvaluationProvider provider = new DeterministicLocalEvaluationProvider();

	@Test
	void returnsTheRenderedPromptUnchangedAcrossInvocations() {
		String renderedPrompt = "Classify this ticket:\nCannot sign in — account locked. {{literal}}";

		String firstOutput = provider.generate(renderedPrompt);
		String secondOutput = provider.generate(renderedPrompt);

		assertThat(firstOutput).isEqualTo(renderedPrompt);
		assertThat(secondOutput).isEqualTo(firstOutput);
	}

	@Test
	void isAvailableThroughTheProviderAbstraction() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.scan(DeterministicLocalEvaluationProvider.class.getPackageName());
			context.refresh();

			assertThat(context.getBean(EvaluationProvider.class))
					.isInstanceOf(DeterministicLocalEvaluationProvider.class);
		}
	}

	@Test
	void rejectsANullRenderedPrompt() {
		assertThatNullPointerException()
				.isThrownBy(() -> provider.generate(null))
				.withMessage("renderedPrompt must not be null");
	}
}
