package karsch.lukas.features.professor.command;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ProfessorLookupRepository extends JpaRepository<ProfessorLookupEntity, UUID> {
}
