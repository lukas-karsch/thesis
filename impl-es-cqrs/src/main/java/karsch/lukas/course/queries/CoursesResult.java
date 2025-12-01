package karsch.lukas.course.queries;

import karsch.lukas.course.CourseDTO;

import java.util.Set;

public record CoursesResult(Set<CourseDTO> courses) {
}
