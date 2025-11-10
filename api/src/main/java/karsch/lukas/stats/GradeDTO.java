package karsch.lukas.stats;

import karsch.lukas.lecture.SimpleLectureDTO;

public record GradeDTO(int grade, int credits, SimpleLectureDTO lecture) {
}
