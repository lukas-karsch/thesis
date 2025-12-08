package karsch.lukas.e2e.lectures;

import karsch.lukas.AxonTestcontainerConfiguration;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.professor.api.CreateProfessorCommand;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.lectures.AbstractLecturesE2ETest;
import karsch.lukas.time.DateTimeProvider;
import karsch.lukas.uuid.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.Collections;
import java.util.UUID;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import({PostgresTestcontainerConfiguration.class, AxonTestcontainerConfiguration.class})
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
    }

    @Override
    public void setSystemTime(Clock clock) {
        dateTimeProvider.setClock(clock);
    }

    @Override
    protected CreateCourseSeedData createCourseSeedData() {
        var courseId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateCourseCommand(courseId, "Test course", null, 5, Collections.emptySet(), 0));

        var professorId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateProfessorCommand(professorId, "Mr.", "Bean"));

        return new CreateCourseSeedData(courseId, professorId);
    }

    @Override
    protected LectureSeedData createLectureSeedData(int minimumCreditsRequired) {
        return null;
    }

    @Override
    protected OverlappingLecturesSeedData createOverlappingLecturesSeedData() {
        return null;
    }

    @Override
    protected SecondProfessorSeedData createSecondProfessorSeedData() {
        return null;
    }

    @Override
    protected AssignGradeSeedData createAssignGradeSeedData(LectureSeedData lectureSeedData, TimeSlot assessmentTimeSlot) {
        return null;
    }

    @Override
    protected UUID createStudent(int semester) {
        return null;
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
