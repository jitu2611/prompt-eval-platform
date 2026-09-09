ALTER TABLE evaluation_runs ADD COLUMN pass_rate DOUBLE PRECISION;
ALTER TABLE evaluation_runs ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE evaluation_case_results (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES evaluation_runs(id) ON DELETE CASCADE,
    evaluation_case_id UUID NOT NULL REFERENCES evaluation_cases(id),
    case_number INTEGER NOT NULL,
    rendered_prompt TEXT NOT NULL,
    provider_output TEXT NOT NULL,
    latency_ms BIGINT NOT NULL,
    passed BOOLEAN NOT NULL,
    CONSTRAINT uk_evaluation_case_results_run_case UNIQUE (run_id, case_number),
    CONSTRAINT chk_evaluation_case_results_latency CHECK (latency_ms >= 0)
);

CREATE INDEX idx_evaluation_case_results_run ON evaluation_case_results (run_id);
