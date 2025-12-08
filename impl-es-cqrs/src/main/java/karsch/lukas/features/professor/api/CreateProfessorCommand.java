package karsch.lukas.features.professor.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record CreateProfessorCommand(@TargetAggregateIdentifier UUID id, String firstName, String lastName) {
}
