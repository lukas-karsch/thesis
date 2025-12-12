package karsch.lukas.features.lectures.command.lookup.lecture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface LectureLookupRepository extends JpaRepository<LectureLookupEntity, UUID> {
}
