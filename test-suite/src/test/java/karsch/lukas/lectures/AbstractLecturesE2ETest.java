package karsch.lukas.lectures;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import karsch.lukas.BaseE2ETest;
import karsch.lukas.lecture.*;
import karsch.lukas.stats.AssessmentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static karsch.lukas.helper.AuthHelper.getProfessorAuthHeader;
import static karsch.lukas.helper.AuthHelper.getStudentAuthHeader;
import static org.hamcrest.Matchers.*;

public abstract class AbstractLecturesE2ETest implements BaseE2ETest {

    private static final Logger log = LoggerFactory.getLogger(AbstractLecturesE2ETest.class);

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + getPort();
    }

    @AfterEach
    void tearDown() {
        resetDatabase();
    }

    public record CreateCourseSeedData(UUID courseId, UUID professorId) {
    }

    /**
     * Must create a course and a professor.
     */
    protected abstract CreateCourseSeedData createCourseSeedData();

    @Test
    @DisplayName("Creating a lecture from a course should return 201")
    void createLectureFromCourse_shouldReturn201() {
        var seedData = createCourseSeedData();

        var createLectureRequest = new CreateLectureRequest(seedData.courseId(), 5, List.of(
                new TimeSlot(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0), LocalTime.of(11, 30))
        ));

        given()
                .when()
                .header(getProfessorAuthHeader(seedData.professorId()))
                .queryParam("courseId", seedData.courseId())
                .body(createLectureRequest)
                .contentType(ContentType.JSON)
                .post("/lectures/create")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Creating a lecture with overlapping timeslots should return 400")
    void creatingLectureWithOverlappingTimeslots_shouldReturn400() {
        var seedData = createCourseSeedData();

        var createLectureRequest = new CreateLectureRequest(seedData.courseId(), 5, List.of(
                new TimeSlot(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0), LocalTime.of(11, 30)),
                new TimeSlot(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0), LocalTime.of(11, 30))
        ));

        given()
                .when()
                .header(getProfessorAuthHeader(seedData.professorId()))
                .queryParam("courseId", seedData.courseId())
                .body(createLectureRequest)
                .contentType(ContentType.JSON)
                .post("/lectures/create")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Creating a lecture from a non-existent course should return 404")
    void createLectureFromCourse_shouldReturn404_ifCourseDoesNotExist() {
        var seedData = createCourseSeedData();

        var uuid = UUID.randomUUID();

        given()
                .when()
                .header(getProfessorAuthHeader(seedData.professorId()))
                .queryParam("courseId", uuid)
                .body(new CreateLectureRequest(uuid, 5, List.of(
                        new TimeSlot(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0), LocalTime.of(11, 30))
                )))
                .contentType(ContentType.JSON)
                .post("/lectures/create")
                .then()
                .statusCode(404);
    }

    public record LectureSeedData(UUID lectureId, UUID studentId, UUID professorId) {
    }

    /**
     * <ul>
     *     <li>Must create a professor.</li>
     *     <li>The professor must create a lecture that is open for enrollment.</li>
     *     <li>The lecture must have timeslots on december 1st and 2nd each</li>
     *     <li>The lecture must have space for 1 student</li>
     *     <li>Must create a student in semester 1 that can enroll to the lecture.</li>
     * </ul>
     */
    protected abstract LectureSeedData createLectureSeedData(int minimumCreditsRequired);

    /**
     * @see #createLectureSeedData(int)
     */
    protected LectureSeedData createLectureSeedData() {
        return createLectureSeedData(0);
    }

    @Test
    @DisplayName("Enrolling in a lecture, then getting the enrolled lecture, then disenrolling should return 201, 200, 200 respectively")
    void enroll_thenGet_thenDisenroll() {
        var lectureSeedData = createLectureSeedData();

        // enroll
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201)
                .body("data.enrollmentStatus", equalToIgnoringCase("ENROLLED"));

        // check enrollments
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .queryParam("studentId", lectureSeedData.studentId())
                .get("/lectures")
                .then()
                .statusCode(200)
                .body("data.enrolled", hasSize(1));

        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .delete("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(200);

        given()
                .when()
                .queryParam("studentId", lectureSeedData.studentId())
                .get("/lectures")
                .then()
                .statusCode(200)
                .body("data.enrolled", hasSize(0));
    }

    @Test
    @DisplayName("Enrolling in a lecture that is not open for enrollment should return 400")
    void enroll_shouldReturn400_ifLectureNotOpenForEnrollment() {
        var lectureSeedData = createLectureSeedData();

        // advance lifecycle
        given()
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.IN_PROGRESS)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // enroll
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Enrolling in a lecture twice should return 400")
    void enroll_shouldReturn400_ifAlreadyEnrolled() {
        var lectureSeedData = createLectureSeedData();

        // enroll
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // enrolling again should error
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    public record OverlappingLecturesSeedData(UUID studentId, UUID lecture1Id, UUID lecture2Id) {
    }

    /**
     * Must create a student.
     * Must create two lectures with overlapping timeslots
     */
    protected abstract OverlappingLecturesSeedData createOverlappingLecturesSeedData();

    @Test
    @DisplayName("Enrolling to several lectures with overlapping time slots is not allowed")
    void enroll_shouldReturn400_ifTimeslotsOverlap() {
        var seedData = createOverlappingLecturesSeedData();

        given()
                .when()
                .header(getStudentAuthHeader(seedData.studentId()))
                .post("/lectures/{lectureId}/enroll", seedData.lecture1Id())
                .then()
                .statusCode(201);

        given()
                .header(getStudentAuthHeader(seedData.studentId()))
                .post("/lectures/{lectureId}/enroll", seedData.lecture2Id())
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("Disenrolling from a finished or archived lecture should have no effect")
    void disenroll_shouldHaveNoEffect_ifLectureIsFinished() {
        var lectureSeedData = createLectureSeedData();

        // enroll
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        Runnable disenroll = () -> given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .delete("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(200);

        // advance lifecycle to finished
        given()
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.FINISHED)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // disenroll
        disenroll.run();

        // check enrollments
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .queryParam("studentId", lectureSeedData.studentId())
                .get("/lectures")
                .then()
                .statusCode(200)
                .body("data.enrolled", hasSize(1));
    }

    @Test
    @DisplayName("Getting lecture details for an existing lecture should return 200")
    void getLectureDetails_shouldReturn200() {
        var lectureSeedData = createLectureSeedData();

        given()
                .when()
                .get("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(200)
                .body("data.dates", hasSize(2))
                .body("data.dates[0].date", containsString("2025-12-01"))
                .body("data.dates[1].date", containsString("2025-12-02"));
    }

    @Test
    @DisplayName("Getting lecture details for a non-existent lecture should return 404")
    void getLectureDetails_shouldReturn404_ifLectureDoesNotExist() {
        given()
                .when()
                .get("/lectures/{lectureId}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Adding dates to a lecture should return 201")
    void addDatesToLecture_shouldReturn201() {
        var lectureSeedData = createLectureSeedData();

        var request = new AssignDatesToLectureRequest(
                Collections.singleton(new TimeSlot(LocalDate.of(2025, 1, 1), LocalTime.of(10, 0), LocalTime.of(12, 0)))
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}/dates", lectureSeedData.lectureId())
                .then()
                .statusCode(201);
    }

    public record SecondProfessorSeedData(UUID professorId) {
    }

    protected abstract SecondProfessorSeedData createSecondProfessorSeedData();

    @Test
    @DisplayName("Adding dates to a lecture by a professor that does not own it should return 403")
    void addDatesToLecture_shouldReturn403_ifNotOwnedByProfessor() {
        var lectureSeedData = createLectureSeedData();
        var professor2Id = createSecondProfessorSeedData().professorId();

        var request = new AssignDatesToLectureRequest(
                Collections.singleton(new TimeSlot(LocalDate.of(2025, 1, 1), LocalTime.of(10, 0), LocalTime.of(12, 0)))
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(professor2Id))
                .post("/lectures/{lectureId}/dates", lectureSeedData.lectureId())
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Adding overlapping dates to a lecture should return 400")
    void addOverlappingDatesToLecture_shouldReturn404() {
        var lectureSeedData = createLectureSeedData();

        var request = new AssignDatesToLectureRequest(Set.of(
                new TimeSlot(LocalDate.of(2025, 1, 1), LocalTime.of(10, 0), LocalTime.of(12, 0)),
                new TimeSlot(LocalDate.of(2025, 1, 1), LocalTime.of(11, 0), LocalTime.of(12, 0))
        ));


        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}/dates", lectureSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    public record AssignGradeSeedData(UUID assessmentId) {
    }

    /**
     * Must create an assessment from the assessmentTimeSlot.
     * weight=1
     * assessmentType=EXAM
     */
    protected abstract AssignGradeSeedData createAssignGradeSeedData(LectureSeedData lectureSeedData, TimeSlot assessmentTimeSlot);

    @Test
    @DisplayName("Assigning a grade to an enrolled student should return 201 and updating it should return 200")
    void assignGrade_shouldReturn201_ifStudentIsEnrolled() {
        // System date is 30.11.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 30, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // assessment takes place at at 01.12.2025
        TimeSlot assessmentTimeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        var lectureSeedData = createLectureSeedData();
        var assignGradeSeedData = createAssignGradeSeedData(lectureSeedData, assessmentTimeSlot);

        // set system date to 02.12.2025 after creating the assessment
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        var request = new AssignGradeRequest(lectureSeedData.studentId(), assignGradeSeedData.assessmentId(), 90);

        Supplier<Response> assignGradeRequest = () -> given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}", lectureSeedData.lectureId());

        // should error because the student is not enrolled to the lecture
        assignGradeRequest.get()
                .then()
                .statusCode(404);

        // let's enroll the student
        given()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // Then, set the lecture to FINISHED so it's allowed to assign grades (IN_PROGRESS is also valid)
        given()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.FINISHED)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // now, lets change the time and check
        // System date is 30.11.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 30, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // should error because the assessment has not happened yet
        assignGradeRequest.get()
                .then()
                .statusCode(400);

        // Set the time back
        // System date is 02.12.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // try again
        assignGradeRequest.get()
                .then()
                .statusCode(201);

        // now lets update
        var updateRequest = new AssignGradeRequest(lectureSeedData.studentId(), assignGradeSeedData.assessmentId(), 100);
        given()
                .body(updateRequest)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .patch("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("It is not allowed to assign grades when the lecture status is not set to FINISHED")
    void assignGrade_isNotAllowed_withInvalidLectureStatus() {
        // System date is 30.11.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 30, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // assessment happens on 1.12.2025
        TimeSlot assessmentTimeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        var lectureSeedData = createLectureSeedData();
        var assignGradeSeedData = createAssignGradeSeedData(lectureSeedData, assessmentTimeSlot);

        // System date is 02.12.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        var request = new AssignGradeRequest(lectureSeedData.studentId(), assignGradeSeedData.assessmentId(), 90);

        // let's enroll the student
        given()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Updating a grade that does not exist should return 404")
    void updateGrade_shouldReturn404_ifNoGradeExists() {
        var lectureSeedData = createLectureSeedData();

        var request = new AssignGradeRequest(lectureSeedData.studentId(), UUID.randomUUID(), 95);

        // should error if no grade exists
        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .patch("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Assigning a grade by a professor that does not own the lecture should return 403")
    void assignGrade_shouldReturn403_ifNotOwnedByProfessor() {
        // System date is 30.11.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 30, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // assessment takes place at 01.12.2025
        TimeSlot assessmentTimeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        var lectureSeedData = createLectureSeedData();
        var assignGradeSeedData = createAssignGradeSeedData(lectureSeedData, assessmentTimeSlot);
        var professor2Id = createSecondProfessorSeedData().professorId();

        // System date is 02.12.2025 after creating the assessment
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        var request = new AssignGradeRequest(lectureSeedData.studentId(), assignGradeSeedData.assessmentId(), 90);

        // enroll the student
        given()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(professor2Id))
                .post("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Assigning a grade for a non-existent assessment should return 404")
    void assignGrade_shouldReturn404_ifAssessmentDoesNotExist() {
        // System date is 02.12.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        var lectureSeedData = createLectureSeedData();

        var request = new AssignGradeRequest(lectureSeedData.studentId(), UUID.randomUUID(), 90);

        // enroll the student
        given()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // set lecture to FINISHED
        given()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.FINISHED)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Updating a grade by a professor that does not own the lecture should return 403")
    void updateGrade_shouldReturn403_ifNotOwnedByProfessor() {
        // System date is 30.11.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 30, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // assessment on 01.12.2025
        TimeSlot assessmentTimeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        var lectureSeedData = createLectureSeedData();
        var assignGradeSeedData = createAssignGradeSeedData(lectureSeedData, assessmentTimeSlot);
        var professor2Id = createCourseSeedData().professorId();

        // System date is 02.12.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        var request = new AssignGradeRequest(lectureSeedData.studentId(), assignGradeSeedData.assessmentId(), 90);

        // enroll the student
        given()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // Then, set the lecture to FINISHED so it's allowed to assign grades
        given()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.FINISHED)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // assign grade
        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // update grade with wrong professor
        var updateRequest = new AssignGradeRequest(lectureSeedData.studentId(), assignGradeSeedData.assessmentId(), 100);
        given()
                .body(updateRequest)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(professor2Id))
                .patch("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Adding an assessment for a non-existing lecture should return 404.")
    void addAssessmentForLecture_should404_ifLectureNotExists() {
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 1, 12, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        ));

        var request = new CreateLectureAssessmentRequest(
                AssessmentType.EXAM,
                new TimeSlot(LocalDate.of(2025, 12, 2), LocalTime.of(12, 0), LocalTime.of(14, 0)),
                1
        );

        var uuid = UUID.randomUUID();
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .header(getProfessorAuthHeader(UUID.randomUUID()))
                .post("/lectures/{lectureId}/assessments", uuid)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Adding an assessment for a lecture should return 201 and the assessment should be visible in the lecture details")
    void addAssessmentForLecture_shouldReturn201() {
        // set time to 1.11.2025, 12:00
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 1, 12, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        ));

        var lectureSeedData = createLectureSeedData();

        var request = new CreateLectureAssessmentRequest(
                // assessment date is in the future
                AssessmentType.EXAM, new TimeSlot(LocalDate.of(2025, 12, 1), LocalTime.of(10, 0), LocalTime.of(12, 0)), 1f
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}/assessments", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(200)
                .body("data.assessments", hasSize(1));
    }

    @Test
    @DisplayName("Adding an assessment with a date in the past should return 400")
    void addAssessmentForLecture_shouldReturn400_ifDateIsInThePast() {
        // set time to 1.11.2025, 12:00
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 1, 12, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        ));

        var lectureSeedData = createLectureSeedData();

        var request = new CreateLectureAssessmentRequest(
                // date is in the past
                AssessmentType.EXAM, new TimeSlot(LocalDate.of(2025, 1, 1), LocalTime.of(10, 0), LocalTime.of(12, 0)), 1f
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}/assessments", lectureSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Adding an assessment to a lecture by a professor that does not own it should return 403")
    void addAssessmentForLecture_shouldReturn403_ifNotOwnedByProfessor() {
        // set time to 1.11.2025, 12:00
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 11, 1, 12, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        ));

        var lectureSeedData = createLectureSeedData();
        var professor2Id = createCourseSeedData().professorId();

        var request = new CreateLectureAssessmentRequest(
                // assessment date is in the future
                AssessmentType.EXAM, new TimeSlot(LocalDate.of(2025, 12, 1), LocalTime.of(10, 0), LocalTime.of(12, 0)), 1f
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(getProfessorAuthHeader(professor2Id))
                .post("/lectures/{lectureId}/assessments", lectureSeedData.lectureId())
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Enrolling a student when a lecture is full should add them to the waiting list and getting the waiting list should return 200")
    void shouldWaitlistStudent_ifLectureIsFull_getWaitingListForLecture_shouldReturn200() {
        var lectureSeedData = createLectureSeedData();
        UUID anotherStudentId = createStudent(1);

        // enroll the first student
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201)
                .body("data.enrollmentStatus", equalToIgnoringCase("ENROLLED"));

        // try to enroll the second student (they should be waitlisted)
        given()
                .when()
                .header(getStudentAuthHeader(anotherStudentId))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201)
                .body("data.enrollmentStatus", equalToIgnoringCase("WAITLISTED"));

        given()
                .when()
                .get("/lectures/{lectureId}/waitingList", lectureSeedData.lectureId())
                .then()
                .statusCode(200)
                .body("data.waitlistEntries", hasSize(1));

        given()
                .when()
                .queryParam("studentId", anotherStudentId)
                .get("/lectures")
                .then()
                .statusCode(200)
                .body("data.waitlisted", hasSize(1));
    }

    @Test
    @DisplayName("Getting the waitlist for a non-existent lecture should return 404")
    void getWaitlistForLecture_shouldReturn404_ifLectureDoesNotExist() {
        given()
                .when()
                .get("/lectures/{lectureId}/waitingList", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    /**
     * Must create a student that is in the specified semester
     *
     * @return the created student's ID
     */
    protected abstract UUID createStudent(int semester);

    @Test
    @DisplayName("When a student disenrolls, the next eligible student from the waitlist should be enrolled to the lecture")
    void eligibleStudentIsEnrolledToLecture_whenSomeoneDisenrolls() {
        var lectureSeedData = createLectureSeedData();
        UUID lowerSemesterStudentId = createStudent(1);
        UUID higherSemesterStudentId = createStudent(5);

        final BiConsumer<UUID, String> doEnrollment = (studentId, expectedEnrollmentStatus) -> given()
                .when()
                .header(getStudentAuthHeader(studentId))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201)
                .body("data.enrollmentStatus", equalToIgnoringCase(expectedEnrollmentStatus));

        // enroll the first student
        doEnrollment.accept(lectureSeedData.studentId(), "ENROLLED");

        // try to enroll the lower semester student (they should be waitlisted)
        doEnrollment.accept(lowerSemesterStudentId, "WAITLISTED");

        // try to enroll the higher semester student (they should be waitlisted)
        doEnrollment.accept(higherSemesterStudentId, "WAITLISTED");

        // student disenrolls
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .delete("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(200);

        // lower semester student is still waitlisted
        given()
                .when()
                .queryParam("studentId", lowerSemesterStudentId)
                .get("/lectures")
                .then()
                .statusCode(200)
                .body("data.waitlisted", hasSize(1))
                .body("data.enrolled", hasSize(0));

        // higher semester student was enrolled
        given()
                .when()
                .queryParam("studentId", higherSemesterStudentId)
                .get("/lectures")
                .then()
                .statusCode(200)
                .body("data.waitlisted", hasSize(0))
                .body("data.enrolled", hasSize(1));
    }

    public record CourseWithPrerequisitesSeedData(UUID lectureId, UUID prerequisiteLectureId, UUID studentId) {
    }

    protected abstract CourseWithPrerequisitesSeedData createCourseAndLectureWithPrerequisites(boolean prerequisitePassed);

    @Test
    @DisplayName("Student should not be able to enroll to a lecture if they haven't completed the prerequisites")
    void studentShouldNotBeAbleToEnroll_ifPrerequisitesAreNotCompleted() {
        var prerequisitesSeedData = createCourseAndLectureWithPrerequisites(false);

        given()
                .when()
                .header(getStudentAuthHeader(prerequisitesSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", prerequisitesSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Student should be able to enroll to a lecture if they completed the prerequisites")
    void studentShouldBeAbleToEnroll_ifPrerequisitesCompleted() {
        var prerequisitesSeedData = createCourseAndLectureWithPrerequisites(true);

        given()
                .when()
                .header(getStudentAuthHeader(prerequisitesSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", prerequisitesSeedData.lectureId())
                .then()
                .statusCode(201)
                .body("data.enrollmentStatus", equalToIgnoringCase("ENROLLED"));
    }

    @Test
    @DisplayName("Student should not be able to enroll if minimum credits are not met")
    void studentShouldNotBeAbleToEnroll_ifMinimumCreditsAreNotMet() {
        var lectureSeedData = createLectureSeedData(5);

        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    public record LectureWithMinimumCredits(UUID lectureId) {
    }

    protected abstract LectureWithMinimumCredits createAssessmentAndGrade(UUID lectureId, UUID studentId);

    @Test
    @DisplayName("Student should be able to enroll if minimum credits are met.")
    void studentShouldBeAbleToEnroll_ifMinimumCreditsAreMet() {
        var passedLectureSeed = createLectureSeedData();
        var newLecture = createAssessmentAndGrade(passedLectureSeed.lectureId(), passedLectureSeed.studentId());

        given()
                .when()
                .header(getStudentAuthHeader(passedLectureSeed.studentId()))
                .post("/lectures/{lectureId}/enroll", newLecture.lectureId())
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Advancing the lifecycle of a lecture should return 201")
    void advanceLifecycleOfLecture_shouldReturn201() {
        var lectureSeedData = createLectureSeedData();

        given()
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.IN_PROGRESS)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Advancing the lifecycle of a lecture to IN_PROGRESS or greater should delete all waitlist entries")
    void advanceLifecycleOfLecture_beyondInProgress_shouldClearWaitlist() {
        var lectureSeedData = createLectureSeedData();
        var student2 = createStudent(1);

        // enroll first student
        given()
                .when()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201)
                .body("data.enrollmentStatus", equalToIgnoringCase("ENROLLED"));

        // second student is waitlisted
        given()
                .when()
                .header(getStudentAuthHeader(student2))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
                .then()
                .statusCode(201)
                .body("data.enrollmentStatus", equalToIgnoringCase("WAITLISTED"));

        // set lecture to IN_PROGRESS
        given()
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.IN_PROGRESS)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(201);

        // student is no longer on the waitlist
        given()
                .when()
                .queryParam("studentId", student2)
                .get("/lectures")
                .then()
                .statusCode(200)
                .body("data.waitlisted", hasSize(0));
    }

    @Test
    @DisplayName("Advancing the lifecycle of a lecture with an invalid status transition should return 400")
    void advanceLifecycleOfLecture_shouldReturn400_ifLifecycleInvalid() {
        var lectureSeedData = createLectureSeedData();

        // setting the lifecycle to DRAFT when it is already OPEN_FOR_ENROLLMENT is not allowed.
        given()
                .when()
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .queryParam("newLectureStatus", LectureStatus.DRAFT)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Advancing the lifecycle of a lecture by a professor that does not own it should return 403")
    void advanceLifecycleOfLecture_shouldReturn403_ifNotOwnedByProfessor() {
        var lectureSeedData = createLectureSeedData();
        var professor2Id = createCourseSeedData().professorId();

        given()
                .when()
                .header(getProfessorAuthHeader(professor2Id))
                .queryParam("newLectureStatus", LectureStatus.IN_PROGRESS)
                .post("/lectures/{lectureId}/lifecycle", lectureSeedData.lectureId())
                .then()
                .statusCode(403);
    }
}
