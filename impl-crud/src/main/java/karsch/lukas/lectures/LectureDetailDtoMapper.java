package karsch.lukas.lectures;

import karsch.lukas.courses.CourseDtoMapper;
import karsch.lukas.lecture.LectureDetailDTO;
import karsch.lukas.mappers.Mapper;
import karsch.lukas.time.TimeSlotMapper;
import karsch.lukas.users.ProfessorDtoMapper;
import karsch.lukas.users.StudentDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureDetailDtoMapper implements Mapper<LectureEntity, LectureDetailDTO> {

    private final CourseDtoMapper courseDtoMapper;
    private final ProfessorDtoMapper professorDtoMapper;
    private final StudentDtoMapper studentDtoMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final WaitlistEntryMapper waitlistEntryMapper;
    private final LectureAssessmentMapper lectureAssessmentMapper;

    @Override
    public LectureDetailDTO map(LectureEntity lectureEntity) {
        return new LectureDetailDTO(
                lectureEntity.getId(),
                courseDtoMapper.map(lectureEntity.getCourse()),
                lectureEntity.getMaximumStudents(),
                timeSlotMapper.mapToList(lectureEntity.getTimeSlots()),
                professorDtoMapper.map(lectureEntity.getProfessor()),
                studentDtoMapper.map(lectureEntity.getEnrolledStudents()),
                waitlistEntryMapper.mapToList(lectureEntity.getWaitlist()),
                lectureEntity.getLectureStatus(),
                lectureAssessmentMapper.map(lectureEntity.getAssessments())
        );
    }
}
