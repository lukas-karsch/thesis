package karsch.lukas.features.stats.query.grades;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class GradedAssessmentId {

    private UUID assessmentId;

    private UUID studentId;

    public static GradedAssessmentId from(UUID assessmentId, UUID studentId) {
        return new GradedAssessmentId(assessmentId, studentId);
    }

}
