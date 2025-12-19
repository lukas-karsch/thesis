package karsch.lukas.features.stats.queries.grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface SimpleCourseProjectionRepository extends JpaRepository<SimpleCourseProjectionEntity, UUID> {
}
