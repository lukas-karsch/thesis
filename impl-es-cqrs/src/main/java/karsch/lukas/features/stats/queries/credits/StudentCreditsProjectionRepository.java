package karsch.lukas.features.stats.queries.credits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface StudentCreditsProjectionRepository extends JpaRepository<StudentCreditsProjectionEntity, UUID> {
}
