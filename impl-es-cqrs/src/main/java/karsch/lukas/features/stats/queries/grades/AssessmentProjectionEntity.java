package karsch.lukas.features.stats.queries.grades;

import jakarta.persistence.*;
import karsch.lukas.stats.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "grades.AssessmentProjectionEntity")
@Table(
        name = "assessment_projection",
        indexes = {
                @Index(name = "assessment__idx_lecture_id", columnList = "lecture_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssessmentProjectionEntity {

    @Id
    private UUID id;

    @Column(name = "lecture_id")
    private UUID lectureId;

    private AssessmentType assessmentType;

    private float weight;

}
