package karsch.lukas.features.course.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
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
}
