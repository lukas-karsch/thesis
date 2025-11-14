package karsch.lukas.lecture;

import karsch.lukas.professor.ProfessorDTO;

import java.util.List;

public record LectureDTO(Long id, Long courseId, int maximumStudents, List<TimeSlot> dates, ProfessorDTO professor,
                         LectureStatus status) {
}
