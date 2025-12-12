package karsch.lukas.features.course.queries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface CourseRepository extends JpaRepository<CourseProjectionEntity, UUID> {
}
