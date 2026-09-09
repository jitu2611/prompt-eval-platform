package io.github.jitu2611.prompteval.datasets;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface EvaluationDatasetRepository extends JpaRepository<EvaluationDataset, UUID> {

	@EntityGraph(attributePaths = "cases")
	@Query("select dataset from EvaluationDataset dataset where dataset.id = :datasetId")
	Optional<EvaluationDataset> findWithCasesById(@Param("datasetId") UUID datasetId);
}
