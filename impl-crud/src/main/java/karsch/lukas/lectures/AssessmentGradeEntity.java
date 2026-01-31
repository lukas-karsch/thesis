package karsch.lukas.lectures;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.users.StudentEntity;
import karsch.lukas.uuid.GeneratedUuidV7;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Entity
@Table(
        name = "assessment_grades",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_student_assessment",
                        columnNames = {"student_id", "assessment_id"}
                )
        }
)
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Audited
public class AssessmentGradeEntity extends AuditableEntity {
    @Id
    @GeneratedUuidV7
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private LectureAssessmentEntity lectureAssessment;

    @Column(nullable = false)
    private int grade;

    @Version
    private Long version;
}
