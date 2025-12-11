package karsch.lukas.features.lectures.queries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface LectureDetailRepository extends JpaRepository<LectureDetailProjectionEntity, UUID> {
}
