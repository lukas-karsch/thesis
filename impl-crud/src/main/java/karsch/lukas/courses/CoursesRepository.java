package karsch.lukas.courses;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CoursesRepository extends JpaRepository<CourseEntity, Long> {
    @EntityGraph(attributePaths = {"prerequisites", "courseAssessments"})
    @Query("select c from CourseEntity c")
    Set<CourseEntity> findAllDetailed();

    @EntityGraph(attributePaths = {"prerequisites", "courseAssessments"})
    @Query("select c from CourseEntity c where c.id = :id")
    Optional<CourseEntity> findByIdDetailed(Long id);
}
