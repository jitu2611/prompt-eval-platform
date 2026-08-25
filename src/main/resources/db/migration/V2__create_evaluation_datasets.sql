CREATE TABLE evaluation_datasets (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE evaluation_cases (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES evaluation_datasets(id),
    case_number INTEGER NOT NULL,
    input_variables JSON NOT NULL,
    expected_output TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_evaluation_case_number UNIQUE (dataset_id, case_number)
);
