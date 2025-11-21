package karsch.lukas.e2e.lectures;

import jakarta.persistence.EntityManager;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.courses.CourseEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.lectures.AbstractLecturesE2ETest;
import karsch.lukas.lectures.LectureAssessmentEntity;
import karsch.lukas.lectures.LectureEntity;
import karsch.lukas.stats.AssessmentType;
import karsch.lukas.time.DateTimeProvider;
import karsch.lukas.time.TimeSlotValueObject;
import karsch.lukas.users.ProfessorEntity;
import karsch.lukas.users.StudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.function.Supplier;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(PostgresTestcontainerConfiguration.class)
@Slf4j
public class LecturesE2ETest extends AbstractLecturesE2ETest {

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
    protected CreateCourseSeedData createCourseSeedData() {
        return inTransaction(() -> {
            var course = new CourseEntity();
            course.setName("Mathematics");
            course.setDescription("Mathematics for beginners");
            course.setCredits(4);

            var professor = new ProfessorEntity();
            professor.setFirstName("Mr.");
            professor.setLastName("Bean");

            entityManager.persist(course);
            entityManager.persist(professor);

            return new CreateCourseSeedData(
                    course.getId(),
                    professor.getId()
            );
        });
    }

    @Override
    protected LectureSeedData createLectureSeedData() {
        return inTransaction(() -> {
            var courseSeedData = createCourseSeedData();

            var lecture = new LectureEntity();
            lecture.setCourse(
                    entityManager.getReference(CourseEntity.class, courseSeedData.courseId())
            );

            lecture.setProfessor(
                    entityManager.getReference(ProfessorEntity.class, courseSeedData.professorId())
            );
            lecture.setMaximumStudents(1);
            lecture.setMinimumCreditsRequired(0);
            lecture.setLectureStatus(LectureStatus.OPEN_FOR_ENROLLMENT);

            entityManager.persist(lecture);

            var student = new StudentEntity();
            student.setFirstName("Hannah");
            student.setLastName("Holzheu");

            entityManager.persist(student);

            return new LectureSeedData(lecture.getId(), student.getId(), courseSeedData.professorId());
        });
    }

    @Override
    protected AssignGradeSeedData createAssignGradeSeedData(LectureSeedData lectureSeedData, TimeSlot assessmentTimeSlot) {
        return inTransaction(() -> {
            var assessment = new LectureAssessmentEntity();
            assessment.setWeight(1);
            assessment.setLecture(entityManager.getReference(LectureEntity.class, lectureSeedData.lectureId()));
            assessment.setTimeSlot(new TimeSlotValueObject(assessmentTimeSlot.date(), assessmentTimeSlot.startTime(), assessmentTimeSlot.endTime()));
            assessment.setAssessmentType(AssessmentType.EXAM);

            entityManager.persist(assessment);

            return new AssignGradeSeedData(assessment.getId());
        });
    }

    @Override
    protected WaitingListSeedData createWaitingListSeedData() {
        return inTransaction(() -> {
            var student2 = new StudentEntity();
            student2.setFirstName("Lukas");
            student2.setLastName("Karsch");

            entityManager.persist(student2);

            return new WaitingListSeedData(student2.getId());
        });
    }

    @Override
    protected SecondProfessorSeedData createSecondProfessorSeedData() {
        return inTransaction(() -> {
            var professor = new ProfessorEntity();
            professor.setFirstName("Funny");
            professor.setLastName("Fox");

            entityManager.persist(professor);

            return new SecondProfessorSeedData(professor.getId());
        });
    }

    private <T> T inTransaction(Supplier<T> supplier) {
        var tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            var result = supplier.get();
            transactionManager.commit(tx);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(tx);
            throw e;
        }
    }

}
