package karsch.lukas.lecture;

import karsch.lukas.student.StudentDTO;

import java.time.LocalDateTime;

public record WaitlistEntryDTO(
        SimpleLectureDTO lecture,
        StudentDTO student,
        LocalDateTime createdAt
) {
}
