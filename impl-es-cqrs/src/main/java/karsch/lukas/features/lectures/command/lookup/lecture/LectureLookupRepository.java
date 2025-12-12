package karsch.lukas.features.lectures.command.lookup.lecture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface LectureLookupRepository extends JpaRepository<LectureLookupEntity, UUID> {
}
