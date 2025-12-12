package karsch.lukas.features.professor.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface ProfessorLookupRepository extends JpaRepository<ProfessorLookupEntity, UUID> {
}
