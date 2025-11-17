package karsch.lukas.lectures;

import karsch.lukas.lecture.WaitlistEntryDTO;
import karsch.lukas.mappers.Mapper;
import karsch.lukas.users.StudentDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitlistEntryMapper implements Mapper<LectureWaitlistEntryEntity, WaitlistEntryDTO> {

    private final SimpleLectureDtoMapper simpleLectureDtoMapper;
    private final StudentDtoMapper studentDtoMapper;

    @Override
    public WaitlistEntryDTO map(LectureWaitlistEntryEntity lectureWaitlistEntryEntity) {
        return new WaitlistEntryDTO(
                simpleLectureDtoMapper.map(lectureWaitlistEntryEntity.getLecture()),
                studentDtoMapper.map(lectureWaitlistEntryEntity.getStudent()),
                lectureWaitlistEntryEntity.getCreatedDate()
        );
    }
}
