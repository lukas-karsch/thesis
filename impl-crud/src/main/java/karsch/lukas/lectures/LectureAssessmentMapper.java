package karsch.lukas.lectures;

import karsch.lukas.lecture.LectureAssessmentDTO;
import karsch.lukas.mappers.Mapper;
import karsch.lukas.time.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureAssessmentMapper implements Mapper<LectureAssessmentEntity, LectureAssessmentDTO> {

    private final SimpleLectureDtoMapper simpleLectureDtoMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public LectureAssessmentDTO map(LectureAssessmentEntity lectureAssessmentEntity) {
        return new LectureAssessmentDTO(
                simpleLectureDtoMapper.map(lectureAssessmentEntity.getLecture()),
                lectureAssessmentEntity.getAssessmentType(),
                timeSlotMapper.map(lectureAssessmentEntity.getTimeSlot()),
                lectureAssessmentEntity.getWeight()
        );
    }
}
