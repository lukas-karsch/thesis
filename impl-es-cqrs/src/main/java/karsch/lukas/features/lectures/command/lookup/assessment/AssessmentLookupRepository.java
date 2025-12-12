package karsch.lukas.features.lectures.command.lookup.assessment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface AssessmentLookupRepository extends JpaRepository<AssessmentLookupEntity, UUID> {
}
