package karsch.lukas.e2e.lectures;

import jakarta.persistence.EntityManager;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.courses.CourseEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.lectures.AbstractLecturesE2ETest;
import karsch.lukas.lectures.AssessmentGradeEntity;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static karsch.lukas.helper.EntityFactory.*;
import static karsch.lukas.helper.TestTransactionHelper.inTransaction;

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
            var course = createCourseEntity(4);

            var professor = createProfessorEntity("Mr.", "Bean");

            entityManager.persist(course);
            entityManager.persist(professor);

            return new CreateCourseSeedData(
                    course.getId(),
                    professor.getId()
            );
        }, transactionManager);
    }

    @Override
    protected LectureSeedData createLectureSeedData(int minimumCreditsRequiredForCourse) {
        return inTransaction(() -> {
            var courseSeedData = createCourseSeedData();

            var lecture = createLectureEntity(
                    entityManager.getReference(ProfessorEntity.class, courseSeedData.professorId()),
                    entityManager.getReference(CourseEntity.class, courseSeedData.courseId())
            );
            lecture.setLectureStatus(LectureStatus.OPEN_FOR_ENROLLMENT);

            var course = entityManager.getReference(CourseEntity.class, courseSeedData.courseId());
            course.setMinimumCreditsRequired(minimumCreditsRequiredForCourse);

            entityManager.persist(course);
            entityManager.persist(lecture);

            var student = createStudentEntity("Hannah", "Holzheu", null);
            entityManager.persist(student);

            return new LectureSeedData(lecture.getId(), student.getId(), courseSeedData.professorId());
        }, transactionManager);
    }

    @Override
    protected CourseWithPrerequisitesSeedData createCourseAndLectureWithPrerequisites(boolean prerequisitePassed) {
        return inTransaction(() -> {
            var prerequisiteCourse = createCourseEntity(5);
            var course = createCourseEntity(6);
            course.getPrerequisites().add(prerequisiteCourse);

            entityManager.persist(prerequisiteCourse);
            entityManager.persist(course);

            var professor = createProfessorEntity();
            var lecture = createLectureEntity(professor, course);
            lecture.setLectureStatus(LectureStatus.OPEN_FOR_ENROLLMENT);

            entityManager.persist(professor);
            entityManager.persist(lecture);

            var student = createStudentEntity();
            entityManager.persist(student);

            var prerequisiteLecture = createLectureEntity(professor, prerequisiteCourse);
            entityManager.persist(prerequisiteLecture);
            prerequisiteLecture.setLectureStatus(LectureStatus.FINISHED);

            if (prerequisitePassed) {
                var assessment = new LectureAssessmentEntity();
                assessment.setLecture(prerequisiteLecture);
                assessment.setAssessmentType(AssessmentType.EXAM);
                assessment.setWeight(1);

                var grade = new AssessmentGradeEntity();
                grade.setLectureAssessment(assessment);
                grade.setStudent(student);
                grade.setGrade(100);

                entityManager.persist(assessment);
                entityManager.persist(grade);
            }

            return new CourseWithPrerequisitesSeedData(lecture.getId(), prerequisiteLecture.getId(), student.getId());
        }, transactionManager);
    }

    @Override
    protected LectureWithMinimumCredits createAssessmentAndGrade(long lectureId, UUID studentId) {
        return inTransaction(() -> {
            var lecture = entityManager.getReference(LectureEntity.class, lectureId);
            var student = entityManager.getReference(StudentEntity.class, studentId);

            var assessment = new LectureAssessmentEntity();
            assessment.setWeight(1);
            assessment.setAssessmentType(AssessmentType.EXAM);
            assessment.setLecture(lecture);

            lecture.setLectureStatus(LectureStatus.FINISHED);

            entityManager.persist(assessment);

            var grade = new AssessmentGradeEntity();
            grade.setStudent(student);
            grade.setLectureAssessment(assessment);
            grade.setGrade(100);

            var newCourse = createCourseEntity(5, "Computer Science", null, 1);
            var newLecture = createLectureEntity(lecture.getProfessor(), newCourse);
            newLecture.setLectureStatus(LectureStatus.OPEN_FOR_ENROLLMENT);

            entityManager.persist(grade);
            entityManager.persist(newCourse);
            entityManager.persist(newLecture);

            return new LectureWithMinimumCredits(newLecture.getId());
        }, transactionManager);
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
        }, transactionManager);
    }

    @Override
    protected SecondProfessorSeedData createSecondProfessorSeedData() {
        return inTransaction(() -> {
            var professor = createProfessorEntity("Funny", "Fox");
            entityManager.persist(professor);
            return new SecondProfessorSeedData(professor.getId());
        }, transactionManager);
    }

    @Override
    protected OverlappingLecturesSeedData createOverlappingLecturesSeedData() {
        return inTransaction(() -> {
            var courseSeedData = createCourseSeedData();
            var professor = entityManager.getReference(ProfessorEntity.class, courseSeedData.professorId());

            var lecture1 = new LectureEntity();
            lecture1.setCourse(entityManager.getReference(CourseEntity.class, courseSeedData.courseId()));
            lecture1.setMaximumStudents(1);
            lecture1.setProfessor(professor);
            lecture1.getTimeSlots().add(new TimeSlotValueObject(
                    LocalDate.of(2025, 11, 1),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0)
            ));
            lecture1.setLectureStatus(LectureStatus.OPEN_FOR_ENROLLMENT);

            var course2 = createCourseEntity(4);
            var lecture2 = new LectureEntity();
            lecture2.setCourse(course2);
            lecture2.setMaximumStudents(1);
            lecture2.setProfessor(professor);
            lecture2.getTimeSlots().add(new TimeSlotValueObject(
                    LocalDate.of(2025, 11, 1),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0)
            ));
            lecture2.setLectureStatus(LectureStatus.OPEN_FOR_ENROLLMENT);

            var student = createStudentEntity("Hannah", "Holzheu", 1);

            entityManager.persist(course2);
            entityManager.persist(lecture1);
            entityManager.persist(lecture2);
            entityManager.persist(student);

            return new OverlappingLecturesSeedData(student.getId(), lecture1.getId(), lecture2.getId());
        }, transactionManager);
    }

    @Override
    protected UUID createStudent(int semester) {
        return inTransaction(() -> {
            var student = new StudentEntity();
            student.setFirstName("Semester");
            student.setLastName("Student");
            student.setSemester(semester);

            entityManager.persist(student);

            return student.getId();
        }, transactionManager);
    }

}
