package karsch.lukas.features.course.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class CourseValidator implements ICourseValidator {

    private final CourseLookupRepository courseLookupRepository;

    @Override
    public boolean allCoursesExist(Collection<UUID> ids) {
        final long count = courseLookupRepository.countAllByIdIn(ids);

        return count == ids.size();
    }

    @Override
    public boolean courseExists(UUID id) {
        return courseLookupRepository.existsById(id);
    }

    @Override
    public int getCreditsForCourse(UUID id) {
        return courseLookupRepository
                .findById(id)
                .orElseThrow()
                .getCredits();
    }

    @Override
    public int getMinimumCreditsToEnroll(UUID id) {
        return courseLookupRepository
                .findById(id)
                .orElseThrow()
                .getMinimumCreditsRequired();
    }

    @Override
    public List<UUID> getPrerequisitesForCourse(UUID id) {
        return courseLookupRepository
                .findById(id)
                .map(CoursesLookupJpaEntity::getPrerequisiteCourses)
                .orElse(Collections.emptyList());
    }
}
