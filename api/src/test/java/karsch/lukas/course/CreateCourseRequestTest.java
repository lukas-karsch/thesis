package karsch.lukas.course;

import karsch.lukas.stats.AssessmentType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateCourseRequestTest {
    @Test
    void shouldNotCreateCourseRequest_ifAssessmentWeightIsNotEqualToOne() {
        assertThatThrownBy(() -> new CreateCourseRequest(
                "Invalid course",
                "Description",
                5,
                Collections.emptyList(),
                List.of(new CourseAssessment(AssessmentType.EXAM, 0.8f))
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
