package karsch.lukas.features.lectures.command.lookup.timeSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LectureTimeslotLookupRepository extends JpaRepository<LectureTimeslotLookupEntity, UUID> {
}
