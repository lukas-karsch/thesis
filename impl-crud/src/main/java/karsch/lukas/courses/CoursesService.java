package karsch.lukas.courses;

import jakarta.transaction.Transactional;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.CreateCourseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class CoursesService {

    private final CoursesRepository coursesRepository;

    private final CourseDtoMapper courseDtoMapper;

    Set<CourseDTO> getAllCourses() {
        var findAll = coursesRepository.findAllDetailed();
        return courseDtoMapper.map(findAll);
    }

    @Transactional
    void createCourse(CreateCourseRequest createCourseRequest) {
        var prerequisites = new HashSet<>(
                coursesRepository.findAllById(createCourseRequest.prerequisiteCourseIds())
        );

        if (prerequisites.size() != createCourseRequest.prerequisiteCourseIds().size()) {
            var notFound = new HashSet<>(createCourseRequest.prerequisiteCourseIds());
            notFound.removeAll(prerequisites.stream().map(CourseEntity::getId).collect(Collectors.toSet()));
            throw new CoursesNotFoundException(notFound);
        }

        var courseEntity = new CourseEntity();
        courseEntity.setName(createCourseRequest.name());
        courseEntity.setDescription(createCourseRequest.description());
        courseEntity.setCredits(createCourseRequest.credits());
        courseEntity.setPrerequisites(prerequisites);
        courseEntity.setCourseAssessments(
                createCourseRequest.assessments()
                        .stream()
                        .map(dto ->
                                new CourseAssessmentValueObject(dto.assessmentType(), dto.weight()))
                        .collect(Collectors.toSet())
        );

        coursesRepository.save(courseEntity);
    }
}
