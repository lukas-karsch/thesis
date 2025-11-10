package karsch.lukas.lecture;

import java.util.List;

public record GetLecturesForStudentResponse(List<LectureDTO> lectures) {
}
