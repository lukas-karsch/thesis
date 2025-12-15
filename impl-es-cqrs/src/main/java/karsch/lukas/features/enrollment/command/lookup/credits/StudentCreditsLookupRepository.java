package karsch.lukas.features.enrollment.command.lookup.credits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface StudentCreditsLookupRepository extends JpaRepository<StudentCreditsLookupProjectionEntity, UUID> {
}
