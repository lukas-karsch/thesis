package karsch.lukas.features.course.commands;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

interface CourseLookupRepository extends JpaRepository<CoursesLookupJpaEntity, UUID> {
    Object countById(UUID id);

    Long countAllByIdIn(Collection<UUID> ids);
}
