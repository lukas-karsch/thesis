package karsch.lukas.features.course.queries;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.FindAllCoursesQuery;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseProjectorTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseProjector courseProjector;

    @Test
    void onCourseCreatedEvent_shouldSaveCourseProjectionEntity() {
        // given
        var event = new CourseCreatedEvent(
                UUID.randomUUID(),
                "Test Course",
                "Test Description",
                5,
                Collections.emptySet(),
                0
        );

        // when
        courseProjector.on(event);

        // then
        var captor = ArgumentCaptor.forClass(CourseProjectionEntity.class);
        verify(courseRepository).save(captor.capture());
        var savedEntity = captor.getValue();

        assertThat(savedEntity.getId()).isEqualTo(event.courseId());
        assertThat(savedEntity.getName()).isEqualTo(event.name());
        assertThat(savedEntity.getDescription()).isEqualTo(event.description());
        assertThat(savedEntity.getCredits()).isEqualTo(event.credits());
        assertThat(savedEntity.getPrerequisiteCourseIds()).isEqualTo(event.prerequisiteCourseIds());
        assertThat(savedEntity.getMinimumCreditsRequired()).isEqualTo(event.minimumCreditsRequired());
    }

    @Test
    void handleFindAllCoursesQuery_shouldReturnAllCourses() {
        // given
        var entity1 = new CourseProjectionEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setName("Course 1");
        entity1.setPrerequisiteCourseIds(Collections.emptySet());

        var entity2 = new CourseProjectionEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setName("Course 2");
        entity2.setPrerequisiteCourseIds(Collections.emptySet());

        when(courseRepository.findAll()).thenReturn(List.of(entity1, entity2));

        // when
        var query = new FindAllCoursesQuery();
        Set<CourseDTO> result = courseProjector.handle(query);

        // then
        assertThat(result).hasSize(2);
        var names = result.stream().map(CourseDTO::name).collect(Collectors.toSet());
        assertThat(names).contains("Course 1", "Course 2");
    }

    @Test
    void findById_whenCourseExists_shouldReturnCourse() {
        // given
        var courseId = UUID.randomUUID();
        var prerequisiteId = UUID.randomUUID();

        var prerequisiteEntity = new CourseProjectionEntity();
        prerequisiteEntity.setId(prerequisiteId);
        prerequisiteEntity.setName("Prerequisite Course");
        prerequisiteEntity.setDescription("Prerequisite Description");
        prerequisiteEntity.setCredits(3);

        var courseEntity = new CourseProjectionEntity();
        courseEntity.setId(courseId);
        courseEntity.setName("Test Course");
        courseEntity.setDescription("Test Description");
        courseEntity.setCredits(5);
        courseEntity.setPrerequisiteCourseIds(Set.of(prerequisiteId));
        courseEntity.setMinimumCreditsRequired(10);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(courseRepository.findAllById(Set.of(prerequisiteId))).thenReturn(List.of(prerequisiteEntity));

        // when
        var query = new FindCourseByIdQuery(courseId);
        CourseDTO result = courseProjector.findById(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(courseId);
        assertThat(result.name()).isEqualTo("Test Course");
        assertThat(result.prerequisites()).hasSize(1);
        SimpleCourseDTO prereqDto = result.prerequisites().iterator().next();
        assertThat(prereqDto.id()).isEqualTo(prerequisiteId);
        assertThat(prereqDto.name()).isEqualTo("Prerequisite Course");
    }

    @Test
    void findById_whenCourseDoesNotExist_shouldReturnNull() {
        // given
        var courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // when
        var query = new FindCourseByIdQuery(courseId);
        CourseDTO result = courseProjector.findById(query);

        // then
        assertThat(result).isNull();
    }
}
