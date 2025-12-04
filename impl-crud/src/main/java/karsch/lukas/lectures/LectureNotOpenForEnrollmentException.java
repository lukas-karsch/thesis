package karsch.lukas.lectures;

import karsch.lukas.lecture.LectureStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

class LectureNotOpenForEnrollmentException extends ResponseStatusException {
    public LectureNotOpenForEnrollmentException(UUID lectureId, LectureStatus lectureStatus) {
        super(HttpStatus.BAD_REQUEST, String.format(
                "Lecture %s with status %s is not open for enrollment",
                lectureId,
                lectureStatus
        ));
    }
}
