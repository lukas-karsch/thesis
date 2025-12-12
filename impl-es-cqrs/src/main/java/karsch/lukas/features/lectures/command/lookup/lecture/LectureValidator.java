package karsch.lukas.features.lectures.command.lookup.lecture;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
class LectureValidator implements ILectureValidator {
    private final LectureLookupRepository lectureLookupRepository;

    @Override
    public Optional<LectureLookupEntity> findById(UUID id) {
        return lectureLookupRepository.findById(id);
    }
}
