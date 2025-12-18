package karsch.lukas.features.stats.queries.gradeHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("gradeHistory.AssessmentProjectionRepository") // avoid bean naming conflict
interface AssessmentProjectionRepository extends JpaRepository<AssessmentProjectionEntity, UUID> {
}
