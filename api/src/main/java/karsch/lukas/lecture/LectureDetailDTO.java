package karsch.lukas.lecture;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.professor.ProfessorDTO;
import karsch.lukas.student.StudentDTO;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record LectureDetailDTO(UUID lectureId, CourseDTO course, int maximumStudents, List<TimeSlot> dates,
                               ProfessorDTO professor, Set<StudentDTO> enrolledStudents,
                               List<WaitlistEntryDTO> waitingList, LectureStatus status,
                               Set<LectureAssessmentDTO> assessments) {
}
