package karsch.lukas.e2e.stats;

import karsch.lukas.AxonTestcontainerConfiguration;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.e2e.config.AxonTestConfiguration;
import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.enrollment.api.AssignGradeCommand;
import karsch.lukas.features.lectures.api.AddAssessmentCommand;
import karsch.lukas.features.lectures.api.AdvanceLectureLifecycleCommand;
import karsch.lukas.features.lectures.api.CreateLectureCommand;
import karsch.lukas.features.lectures.api.EnrollStudentCommand;
import karsch.lukas.features.professor.api.CreateProfessorCommand;
import karsch.lukas.features.student.api.CreateStudentCommand;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.stats.AbstractStatsE2ETest;
import karsch.lukas.stats.AssessmentType;
import karsch.lukas.time.DateTimeProvider;
import karsch.lukas.uuid.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.test.server.AxonServerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.*;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.restassured.RestAssured.given;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import({PostgresTestcontainerConfiguration.class, AxonTestcontainerConfiguration.class, AxonTestConfiguration.class})
@Slf4j
public class EsCqrsStatsE2ETest extends AbstractStatsE2ETest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private DateTimeProvider dateTimeProvider;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private AxonServerContainer axonServerContainer;

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void resetDatabase() {
        try (
                Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()
        ) {
            log.info("Resetting database...");
            var rs = conn.getMetaData().getTables(null, "public", "%", new String[]{"TABLE"});
            while (rs.next()) {
                String table = rs.getString("TABLE_NAME");
                stmt.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
            }
        } catch (SQLException e) {
            log.error("Could not reset database", e);
        }

        given()
                .with()
                .baseUri("http://localhost:" + axonServerContainer.getHttpPort())
                .delete("/v1/devmode/purge-events")
                .then()
                .statusCode(200);
    }

    @Override
    public void setSystemTime(Clock clock) {
        dateTimeProvider.setClock(clock);
    }

    @Override
    protected GradingSeedData createGradingSeedData() {
        setSystemTime(Clock.fixed(LocalDateTime.of(2025, 12, 1, 12, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC")));

        UUID studentId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateStudentCommand(studentId, "Lukas", "Karsch", 1));

        UUID professorId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateProfessorCommand(professorId, "Nö", "!"));

        UUID course1 = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateCourseCommand(course1, "Mathematics", null, 5, Collections.emptySet(), 0));

        UUID course2 = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateCourseCommand(course2, "Computer Science", null, 5, Collections.emptySet(), 0));

        UUID lecture1 = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateLectureCommand(lecture1, course1, 1, Collections.emptyList(), professorId));

        UUID lecture2 = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateLectureCommand(lecture2, course2, 1, Collections.emptyList(), professorId));

        CompletableFuture.allOf(
                commandGateway.send(new AdvanceLectureLifecycleCommand(lecture1, LectureStatus.OPEN_FOR_ENROLLMENT, professorId)),
                commandGateway.send(new AdvanceLectureLifecycleCommand(lecture2, LectureStatus.OPEN_FOR_ENROLLMENT, professorId))
        );

        commandGateway.sendAndWait(new EnrollStudentCommand(lecture1, studentId));
        commandGateway.sendAndWait(new EnrollStudentCommand(lecture2, studentId));

        UUID assessment1 = UuidUtils.randomV7();
        commandGateway.sendAndWait(new AddAssessmentCommand(lecture1, assessment1, new TimeSlot(LocalDate.of(2025, 12, 5), LocalTime.of(10, 0), LocalTime.of(12, 0)), AssessmentType.EXAM, 1, professorId));

        UUID assessment2 = UuidUtils.randomV7();
        commandGateway.sendAndWait(new AddAssessmentCommand(lecture2, assessment2, new TimeSlot(LocalDate.of(2025, 12, 5), LocalTime.of(10, 0), LocalTime.of(12, 0)), AssessmentType.EXAM, 1, professorId));

        CompletableFuture.allOf(
                commandGateway.send(new AdvanceLectureLifecycleCommand(lecture1, LectureStatus.IN_PROGRESS, professorId)),
                commandGateway.send(new AdvanceLectureLifecycleCommand(lecture2, LectureStatus.IN_PROGRESS, professorId))
        ).join();

        setSystemTime(Clock.fixed(LocalDateTime.of(2025, 12, 10, 12, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC")));

        commandGateway.sendAndWait(new AssignGradeCommand(assessment1, lecture1, studentId, 100, professorId));
        commandGateway.sendAndWait(new AssignGradeCommand(assessment2, lecture2, studentId, 0, professorId));

        CompletableFuture.allOf(
                commandGateway.send(new AdvanceLectureLifecycleCommand(lecture1, LectureStatus.FINISHED, professorId)),
                commandGateway.send(new AdvanceLectureLifecycleCommand(lecture2, LectureStatus.FINISHED, professorId))
        ).join();

        return new GradingSeedData(
                studentId,
                lecture1,
                assessment1,
                professorId
        );
    }
}
