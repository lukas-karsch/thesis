package karsch.lukas.features.lectures.command.lookup.timeSlot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LectureTimeslotLookupRepository extends JpaRepository<LectureTimeslotLookupEntity, UUID> {
}
