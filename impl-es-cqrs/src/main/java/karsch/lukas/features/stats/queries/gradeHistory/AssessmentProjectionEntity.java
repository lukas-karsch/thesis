package karsch.lukas.features.stats.queries.gradeHistory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "gradeHistory.AssessmentProjectionEntity")
@Table(
        name = "stats__gradeHistory__assessment_projection",
        indexes = @Index(name = "assessment_projection__idx_lecture_id", columnList = "lecture_id")
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class AssessmentProjectionEntity {
    @Id
    private UUID assessmentId;

    @Column(name = "lecture_id")
    private UUID lectureId;
}
