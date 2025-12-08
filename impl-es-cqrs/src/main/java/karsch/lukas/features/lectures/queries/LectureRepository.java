package karsch.lukas.features.lectures.queries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface LectureRepository extends JpaRepository<LectureProjectionEntity, UUID> {
}
