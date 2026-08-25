package io.github.jitu2611.prompteval.datasets;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface EvaluationDatasetRepository extends JpaRepository<EvaluationDataset, UUID> {
}
