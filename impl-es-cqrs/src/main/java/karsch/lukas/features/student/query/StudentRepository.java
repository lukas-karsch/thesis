package karsch.lukas.features.student.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface StudentRepository extends JpaRepository<StudentProjectionEntity, UUID> {
}
