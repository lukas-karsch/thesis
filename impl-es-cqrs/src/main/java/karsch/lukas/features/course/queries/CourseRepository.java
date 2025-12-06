package karsch.lukas.features.course.queries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CourseRepository extends JpaRepository<CourseProjectionEntity, UUID> {
}
