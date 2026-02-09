package karsch.lukas.features.stats.queries.credits;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CourseProjectionRepository extends JpaRepository<CourseProjectionEntity, UUID> {
}
