package karsch.lukas.features.enrollment.command.lookup.credits;

import karsch.lukas.features.course.commands.ICourseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class StudentCreditsValidator implements IStudentCreditsValidator {

    private final StudentCreditsLookupRepository studentCreditsLookupRepository;

    private final ICourseValidator courseValidator;

    @Override
    public boolean hasEnoughCreditsToEnroll(UUID studentId, UUID courseId) {
        int studentCredits = studentCreditsLookupRepository.findById(studentId).map(StudentCreditsLookupProjectionEntity::getCredits).orElse(0);
        return studentCredits >= courseValidator.getMinimumCreditsToEnroll(courseId);
    }

    @Override
    public boolean hasPassedAllPrerequisites(UUID studentId, UUID courseId) {
        var passedCourses = studentCreditsLookupRepository.findById(studentId).map(StudentCreditsLookupProjectionEntity::getPassedCourses).orElse(Collections.emptyList());
        var coursePrerequisites = courseValidator.getPrerequisitesForCourse(courseId);

        return new HashSet<>(passedCourses).containsAll(coursePrerequisites);
    }
}
