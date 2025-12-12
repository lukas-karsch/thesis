package karsch.lukas.features.student.command.lookup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface StudentLookupRepository extends JpaRepository<StudentLookupEntity, UUID> {
}
