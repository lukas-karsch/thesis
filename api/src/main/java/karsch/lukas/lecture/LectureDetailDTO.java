package karsch.lukas.lecture;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.professor.ProfessorDTO;
import karsch.lukas.student.StudentDTO;

import java.time.LocalDate;
import java.util.List;

public record LectureDetailDTO(Long lectureId, CourseDTO courseDTO, int maximumStudents, List<LocalDate> dates,
                               ProfessorDTO professor, List<StudentDTO> enrolledStudents) {
}
