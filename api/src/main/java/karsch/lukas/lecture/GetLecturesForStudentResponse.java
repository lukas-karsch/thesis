package karsch.lukas.lecture;

import java.util.List;

public record GetLecturesForStudentResponse(List<LectureDTO> enrolled, List<LectureDTO> waitlisted) {
}
