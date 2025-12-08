package karsch.lukas.features.lectures.command;

import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.stats.AssessmentType;
import lombok.AllArgsConstructor;
import org.axonframework.modelling.command.EntityId;

import java.util.UUID;

@AllArgsConstructor
public class AssessmentValueObject {
    @EntityId
    private UUID assessmentId;

    private TimeSlot timeSlot;

    private float weight;

    private AssessmentType assessmentType;
}
