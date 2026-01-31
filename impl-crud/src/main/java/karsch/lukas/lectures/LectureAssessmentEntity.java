package karsch.lukas.lectures;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.stats.AssessmentType;
import karsch.lukas.time.TimeSlotValueObject;
import karsch.lukas.uuid.GeneratedUuidV7;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Entity
@Table(name = "lecture_assessments")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Audited
public class LectureAssessmentEntity extends AuditableEntity {
    @Id
    @GeneratedUuidV7
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_id", nullable = false)
    private LectureEntity lecture;

    private TimeSlotValueObject timeSlot;

    @Column(nullable = false)
    private AssessmentType assessmentType;

    @Column(nullable = false)
    private float weight;

}
