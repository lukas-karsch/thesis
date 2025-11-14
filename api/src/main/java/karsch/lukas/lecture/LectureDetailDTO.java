package karsch.lukas.lecture;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.professor.ProfessorDTO;
import karsch.lukas.student.StudentDTO;

import java.util.List;

public record LectureDetailDTO(Long lectureId, CourseDTO courseDTO, int maximumStudents, List<TimeSlot> dates,
                               ProfessorDTO professor, List<StudentDTO> enrolledStudents, List<StudentDTO> waitingList,
                               LectureStatus status) {
}
