package karsch.lukas.stats;

import karsch.lukas.lectures.AssessmentGradeEntity;
import karsch.lukas.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
class GradedAssessmentDtoMapper implements Mapper<AssessmentGradeEntity, GradedAssessmentDTO> {

    @Override
    public GradedAssessmentDTO map(AssessmentGradeEntity entity) {
        return new GradedAssessmentDTO(
                entity.getLectureAssessment().getId(),
                entity.getLectureAssessment().getAssessmentType(),
                entity.getGrade(),
                entity.getLectureAssessment().getWeight(),
                entity.getCreatedAt()
        );
    }
}
