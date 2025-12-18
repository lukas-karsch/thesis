package karsch.lukas.features.stats.queries.grades;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GradedAssessmentId {

    private UUID assessmentId;

    private UUID studentId;

}
