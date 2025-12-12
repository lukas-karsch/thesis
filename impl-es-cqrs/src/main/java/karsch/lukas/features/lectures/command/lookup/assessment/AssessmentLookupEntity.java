package karsch.lukas.features.lectures.command.lookup.assessment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import karsch.lukas.stats.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "assessment_lookup")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssessmentLookupEntity {
    @Id
    private UUID id;

    private UUID lectureId;

    private float weight;

    private TimeSlotEmbeddable timeSlot;

    private AssessmentType assessmentType;

}
