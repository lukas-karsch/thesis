package karsch.lukas.features.stats.queries.grades;

import jakarta.persistence.*;
import karsch.lukas.stats.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "graded_assessment_projection",
        indexes = {
                @Index(name = "graded_assessment__idx_student_id_lecture_id", columnList = "student_id,lecture_id"),
                @Index(name = "graded_assessment__idx_lecture_id", columnList = "lecture_id"),
                @Index(name = "graded_assessment__idx_student_id", columnList = "student_id"),
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(GradedAssessmentId.class)
public class GradedAssessmentProjectionEntity {

    @Id
    private UUID assessmentId;

    @Id
    @Column(name = "student_id")
    private UUID studentId;

    private AssessmentType assessmentType;

    private int grade;

    private float weight;

    private LocalDateTime assignedAt;

    @Column(name = "lecture_id")
    private UUID lectureId;

}
