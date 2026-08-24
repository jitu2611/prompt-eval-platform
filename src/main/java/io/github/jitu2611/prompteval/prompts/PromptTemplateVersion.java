package io.github.jitu2611.prompteval.prompts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prompt_template_versions", uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "version_number"}))
class PromptTemplateVersion {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "template_id", nullable = false, updatable = false)
	private PromptTemplate template;

	@Column(name = "version_number", nullable = false, updatable = false)
	private int versionNumber;

	@Column(nullable = false, columnDefinition = "TEXT", updatable = false)
	private String content;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected PromptTemplateVersion() {
	}

	PromptTemplateVersion(PromptTemplate template, int versionNumber, String content) {
		this.id = UUID.randomUUID();
		this.template = template;
		this.versionNumber = versionNumber;
		this.content = content;
		this.createdAt = Instant.now();
	}

	UUID getId() {
		return id;
	}

	int getVersionNumber() {
		return versionNumber;
	}

	String getContent() {
		return content;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
