package karsch.lukas.e2e.stats;

import jakarta.persistence.EntityManager;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lectures.AssessmentGradeEntity;
import karsch.lukas.lectures.EnrollmentEntity;
import karsch.lukas.lectures.LectureAssessmentEntity;
import karsch.lukas.stats.AbstractStatsE2ETest;
import karsch.lukas.stats.AssessmentType;
import karsch.lukas.time.DateTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;

import static karsch.lukas.helper.EntityFactory.*;
import static karsch.lukas.helper.TestTransactionHelper.inTransaction;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(PostgresTestcontainerConfiguration.class)
@Slf4j
public class StatsE2ETest extends AbstractStatsE2ETest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DateTimeProvider dateTimeProvider;

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
    protected GradingSeedData createGradingSeedData() {
        return inTransaction(() -> {
            var student = createStudentEntity();
            var professor = createProfessorEntity();

            var course1 = createCourseEntity(5);
            var course2 = createCourseEntity(5);

            var lecture1 = createLectureEntity(professor, course1);
            var lecture1Enrollment = new EnrollmentEntity();
            lecture1Enrollment.setStudent(student);
            lecture1Enrollment.setLecture(lecture1);
            lecture1.getEnrollments().add(lecture1Enrollment);
            lecture1.setLectureStatus(LectureStatus.FINISHED);

            var lecture2 = createLectureEntity(professor, course2);
            var lecture2Enrollment = new EnrollmentEntity();
            lecture2Enrollment.setStudent(student);
            lecture2Enrollment.setLecture(lecture2);
            lecture2.getEnrollments().add(lecture2Enrollment);
            lecture2.setLectureStatus(LectureStatus.FINISHED);

            var assessment1 = new LectureAssessmentEntity();
            assessment1.setAssessmentType(AssessmentType.EXAM);
            assessment1.setLecture(lecture1);
            assessment1.setWeight(1.0f);

            var assessment2 = new LectureAssessmentEntity();
            assessment2.setAssessmentType(AssessmentType.EXAM);
            assessment2.setLecture(lecture2);
            assessment2.setWeight(1.0f);

            lecture1.getAssessments().add(assessment1);
            lecture2.getAssessments().add(assessment2);

            var grade1 = new AssessmentGradeEntity();
            grade1.setGrade(100); // PASSED
            grade1.setStudent(student);
            grade1.setLectureAssessment(assessment1);

            var grade2 = new AssessmentGradeEntity();
            grade2.setGrade(0); // FAILED
            grade2.setStudent(student);
            grade2.setLectureAssessment(assessment2);

            entityManager.persist(student);
            entityManager.persist(professor);
            entityManager.persist(course1);
            entityManager.persist(course2);
            entityManager.persist(lecture1);
            entityManager.persist(lecture2);
            entityManager.persist(grade1);
            entityManager.persist(grade2);

            return new GradingSeedData(student.getId(), lecture1.getId(), assessment1.getId(), professor.getId());
        }, transactionManager);
    }
}
