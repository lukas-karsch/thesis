package karsch.lukas.features.enrollment.query.credits;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentCreditsProjectionRepository extends JpaRepository<StudentCreditsProjectionEntity, UUID> {
}
