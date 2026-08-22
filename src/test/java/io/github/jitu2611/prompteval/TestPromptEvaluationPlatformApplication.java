package io.github.jitu2611.prompteval;

import org.springframework.boot.SpringApplication;

public class TestPromptEvaluationPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(PromptEvaluationPlatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
