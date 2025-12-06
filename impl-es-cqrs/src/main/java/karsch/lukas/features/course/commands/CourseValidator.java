package karsch.lukas.features.course.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class CourseValidator implements ICourseValidator {

    private final CourseLookupRepository courseLookupRepository;

    @Override
    public boolean allCoursesExist(Collection<UUID> ids) {
        final long count = courseLookupRepository.countAllByIdIn(ids);

        return count == ids.size();
    }
}
