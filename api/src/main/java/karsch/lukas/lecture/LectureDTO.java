package karsch.lukas.lecture;

import karsch.lukas.professor.ProfessorDTO;

import java.time.LocalDate;
import java.util.List;

public record LectureDTO(Long id, Long courseId, int maximumStudents, List<LocalDate> dates, ProfessorDTO professor) {
}
