package karsch.lukas.features.stats.query.grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentGradesRepository extends JpaRepository<StudentGradesProjectionEntity, StudentGradesProjectionEntityId> {
    List<StudentGradesProjectionEntity> findByStudentId(UUID studentId);
}
