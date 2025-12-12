package karsch.lukas.features.course.commands;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
interface CourseLookupRepository extends JpaRepository<CoursesLookupJpaEntity, UUID> {
    Object countById(UUID id);

    Long countAllByIdIn(Collection<UUID> ids);
}
