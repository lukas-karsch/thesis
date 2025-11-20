package karsch.lukas.lecture;

import karsch.lukas.student.StudentDTO;

import java.time.LocalDateTime;

public record WaitlistedStudentDTO(StudentDTO student, LocalDateTime waitlistedAt) {
}
