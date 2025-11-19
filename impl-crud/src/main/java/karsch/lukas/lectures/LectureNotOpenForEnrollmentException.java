package karsch.lukas.lectures;

import karsch.lukas.lecture.LectureStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class LectureNotOpenForEnrollmentException extends ResponseStatusException {
    public LectureNotOpenForEnrollmentException(Long lectureId, LectureStatus lectureStatus) {
        super(HttpStatus.BAD_REQUEST, String.format(
                "Lecture %d with status %s is not open for enrollment",
                lectureId,
                lectureStatus
        ));
    }
}
