package karsch.lukas.features.stats.queries.credits;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentCreditsProjectionRepository extends JpaRepository<StudentCreditsProjectionEntity, UUID> {
}
