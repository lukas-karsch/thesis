package karsch.lukas.lectures;

import karsch.lukas.courses.SimpleCourseDtoMapper;
import karsch.lukas.lecture.LectureDTO;
import karsch.lukas.mappers.Mapper;
import karsch.lukas.time.TimeSlotMapper;
import karsch.lukas.users.ProfessorDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureDtoMapper implements Mapper<LectureEntity, LectureDTO> {

    private final SimpleCourseDtoMapper simpleCourseDtoMapper;
    private final ProfessorDtoMapper professorDtoMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public LectureDTO map(LectureEntity lectureEntity) {
        return new LectureDTO(
                lectureEntity.getId(),
                simpleCourseDtoMapper.map(lectureEntity.getCourse()),
                lectureEntity.getMaximumStudents(),
                timeSlotMapper.mapToList(lectureEntity.getTimeSlots()),
                professorDtoMapper.map(lectureEntity.getProfessor()),
                lectureEntity.getLectureStatus()
        );
    }
}
