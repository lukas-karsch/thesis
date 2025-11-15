package karsch.lukas.courses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CoursesRepository extends JpaRepository<CourseEntity, Long> {
}
