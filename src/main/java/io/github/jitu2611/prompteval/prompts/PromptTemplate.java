package io.github.jitu2611.prompteval.prompts;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prompt_templates")
class PromptTemplate {

	@Id
	private UUID id;

	@Column(nullable = false, length = 120, updatable = false)
	private String name;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("versionNumber ASC")
	private List<PromptTemplateVersion> versions = new ArrayList<>();

	protected PromptTemplate() {
	}

	PromptTemplate(String name) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.createdAt = Instant.now();
	}

	PromptTemplateVersion addVersion(String content) {
		PromptTemplateVersion version = new PromptTemplateVersion(this, versions.size() + 1, content);
		versions.add(version);
		return version;
	}

	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	List<PromptTemplateVersion> getVersions() {
		return List.copyOf(versions);
	}
}
