package karsch.lukas.e2e.lectures;

import karsch.lukas.AxonTestcontainerConfiguration;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.e2e.config.AxonTestConfiguration;
import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.lectures.api.AddAssessmentCommand;
import karsch.lukas.features.lectures.api.AdvanceLectureLifecycleCommand;
import karsch.lukas.features.lectures.api.CreateLectureCommand;
import karsch.lukas.features.professor.api.CreateProfessorCommand;
import karsch.lukas.features.student.api.CreateStudentCommand;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.lectures.AbstractLecturesE2ETest;
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
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import({PostgresTestcontainerConfiguration.class, AxonTestcontainerConfiguration.class, AxonTestConfiguration.class})
@Slf4j
public class EsCqrsLecturesE2ETest extends AbstractLecturesE2ETest {

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
    protected CreateCourseSeedData createCourseSeedData() {
        return createCourseSeedData(0);
    }

    private CreateCourseSeedData createCourseSeedData(int minimumCreditsRequired) {
        var courseId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateCourseCommand(courseId, "Test course", null, 5, Collections.emptySet(), minimumCreditsRequired));

        var professorId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateProfessorCommand(professorId, "Mr.", "Bean"));

        return new CreateCourseSeedData(courseId, professorId);
    }

    @Override
    protected LectureSeedData createLectureSeedData(int minimumCreditsRequired) {
        var courseSeedData = createCourseSeedData(minimumCreditsRequired);

        var lectureId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateLectureCommand(
                lectureId,
                courseSeedData.courseId(),
                1,
                List.of(
                        new TimeSlot(LocalDate.of(2025, 12, 1), LocalTime.of(10, 0), LocalTime.of(12, 0)),
                        new TimeSlot(LocalDate.of(2025, 12, 2), LocalTime.of(10, 0), LocalTime.of(12, 0))
                ),
                courseSeedData.professorId()
        ));

        commandGateway.sendAndWait(new AdvanceLectureLifecycleCommand(lectureId, LectureStatus.OPEN_FOR_ENROLLMENT, courseSeedData.professorId()));

        var studentId = createStudent(0);

        return new LectureSeedData(
                lectureId,
                studentId,
                courseSeedData.professorId()
        );
    }

    @Override
    protected OverlappingLecturesSeedData createOverlappingLecturesSeedData() {
        var lecture1 = createLectureSeedData(0);
        var lecture2 = createLectureSeedData(0);

        return new OverlappingLecturesSeedData(lecture1.studentId(), lecture1.lectureId(), lecture2.lectureId());
    }

    @Override
    protected SecondProfessorSeedData createSecondProfessorSeedData() {
        UUID professorId = UuidUtils.randomV7();
        commandGateway.sendAndWait(
                new CreateProfessorCommand(professorId, "Bugs", "Bunny")
        );
        return new SecondProfessorSeedData(professorId);
    }

    @Override
    protected AssignGradeSeedData createAssignGradeSeedData(LectureSeedData lectureSeedData, TimeSlot assessmentTimeSlot) {
        UUID assessmentId = UuidUtils.randomV7();
        commandGateway.sendAndWait(
                new AddAssessmentCommand(lectureSeedData.lectureId(), assessmentId, assessmentTimeSlot, AssessmentType.EXAM, 1, lectureSeedData.professorId())
        );

        return new AssignGradeSeedData(assessmentId);
    }

    @Override
    protected UUID createStudent(int semester) {
        UUID studentId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateStudentCommand(studentId, "Hannah", "Holzheu", semester));

        return studentId;
    }

    @Override
    protected CourseWithPrerequisitesSeedData createCourseAndLectureWithPrerequisites(boolean prerequisitePassed) {
        return null;
    }

    @Override
    protected LectureWithMinimumCredits createAssessmentAndGrade(UUID lectureId, UUID studentId) {
        return null;
    }
}
