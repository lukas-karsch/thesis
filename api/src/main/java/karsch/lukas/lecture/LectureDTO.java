package karsch.lukas.lecture;

import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.professor.ProfessorDTO;

import java.util.List;

public record LectureDTO(
        Long id,
        SimpleCourseDTO course,
        int maximumStudents,
        List<TimeSlot> dates,
        ProfessorDTO professor,
        LectureStatus status
) {
}
