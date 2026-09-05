package io.github.jitu2611.prompteval.runs;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface EvaluationRunRepository extends JpaRepository<EvaluationRun, UUID> {
}
