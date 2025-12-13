package karsch.lukas.features.course.commands;

import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.course.exceptions.MissingCoursesException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourseAggregateTest {

    private FixtureConfiguration<CourseAggregate> fixture;

    private ICourseValidator courseValidator = mock(ICourseValidator.class);

    @BeforeEach
    public void setUp() {
        fixture = new AggregateTestFixture<>(CourseAggregate.class);

        fixture.registerInjectableResource(courseValidator);
    }

    @Test
    void testCreatingCourseWithoutPrerequisites() {
        UUID courseId = UUID.randomUUID();

        when(courseValidator.allCoursesExist(anyCollection())).thenReturn(true);

        fixture.givenNoPriorActivity()
                .when(new CreateCourseCommand(courseId, "Maths", "Description", 5, Collections.emptySet(), 0))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new CourseCreatedEvent(courseId, "Maths", "Description", 5, Collections.emptySet(), 0));
    }

    @Test
    void testCreatingCourseWithMissingPrerequisites() {
        when(courseValidator.allCoursesExist(anyCollection())).thenReturn(false);

        fixture.givenNoPriorActivity()
                .when(new CreateCourseCommand(UUID.randomUUID(), "Maths", "Description", 5, Collections.emptySet(), 0))
                .expectException(MissingCoursesException.class);
    }
}
