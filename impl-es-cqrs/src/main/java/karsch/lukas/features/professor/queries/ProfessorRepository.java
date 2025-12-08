package karsch.lukas.features.professor.queries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ProfessorRepository extends JpaRepository<ProfessorProjectionEntity, UUID> {
}
