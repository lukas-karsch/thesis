package karsch.lukas.dev;

import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.professor.api.CreateProfessorCommand;
import karsch.lukas.time.DateTimeProvider;
import karsch.lukas.uuid.UuidUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.UUID;

@Profile("dev")
@Slf4j
@RequiredArgsConstructor
@Component
public class SeedDataRunner implements CommandLineRunner {

    private final DateTimeProvider dateTimeProvider;
    private final CommandGateway commandGateway;

    @Override
    public void run(String... args) throws Exception {
        dateTimeProvider.setClock(
                Clock.fixed(
                        LocalDateTime.of(2025, 11, 15, 12, 0, 0).toInstant(ZoneOffset.UTC),
                        ZoneId.of("UTC")
                )
        );

        log.info("Seeding data...");

        UUID professorId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        commandGateway.sendAndWait(
                new CreateProfessorCommand(professorId, "Mr.", "Bean")
        );

        UUID courseId = UuidUtils.randomV7();
        commandGateway.send(
                new CreateCourseCommand(courseId, "Mathematics", null, 5, Collections.emptySet(), 0)
        );
    }
}
