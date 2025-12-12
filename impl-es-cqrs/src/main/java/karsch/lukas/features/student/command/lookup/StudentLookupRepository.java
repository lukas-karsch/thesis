package karsch.lukas.features.student.command.lookup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentLookupRepository extends JpaRepository<StudentLookupEntity, UUID> {
}
