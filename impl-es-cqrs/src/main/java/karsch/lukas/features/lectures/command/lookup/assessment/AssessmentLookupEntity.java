package karsch.lukas.features.lectures.command.lookup.assessment;

import jakarta.persistence.*;
import karsch.lukas.core.lookup.TimeSlotEmbeddable;
import karsch.lukas.stats.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "assessment_lookup",
        indexes = {
                @Index(name = "idx_lecture_id", columnList = "lecture_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssessmentLookupEntity {
    @Id
    private UUID id;

    @Column(name = "lecture_id")
    private UUID lectureId;

    private float weight;

    private TimeSlotEmbeddable timeSlot;

    private AssessmentType assessmentType;

}
