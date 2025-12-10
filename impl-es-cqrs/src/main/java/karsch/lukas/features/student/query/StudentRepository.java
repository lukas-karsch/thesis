package karsch.lukas.features.student.query;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<StudentProjectionEntity, UUID> {
}
