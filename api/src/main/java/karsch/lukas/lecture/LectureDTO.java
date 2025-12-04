package karsch.lukas.lecture;

import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.professor.ProfessorDTO;

import java.util.List;
import java.util.UUID;

public record LectureDTO(
        UUID id,
        SimpleCourseDTO course,
        int maximumStudents,
        List<TimeSlot> dates,
        ProfessorDTO professor,
        LectureStatus status
) {
}
