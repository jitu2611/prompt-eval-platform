CREATE TABLE evaluation_runs (
    id UUID PRIMARY KEY,
    prompt_version_id UUID NOT NULL REFERENCES prompt_template_versions(id),
    dataset_id UUID NOT NULL REFERENCES evaluation_datasets(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
