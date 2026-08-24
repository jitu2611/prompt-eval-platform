CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE prompt_template_versions (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES prompt_templates(id),
    version_number INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_prompt_template_version UNIQUE (template_id, version_number)
);
