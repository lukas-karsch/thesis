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

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static karsch.lukas.helper.AuthHelper.getProfessorAuthHeader;
import static karsch.lukas.helper.AuthHelper.getStudentAuthHeader;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;

public abstract class AbstractLecturesE2ETest implements BaseE2ETest {

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + getPort();
    }

    @AfterEach
    void tearDown() {
        resetDatabase();
    }

    public record CreateCourseSeedData(Long courseId, Long professorId) {
    }

    /**
     * Must create a course and a professor.
     */
    protected abstract CreateCourseSeedData createCourseSeedData();

    @Test
    @DisplayName("Creating a lecture from a course should return 201")
    void createLectureFromCourse_shouldReturn201() {
        var seedData = createCourseSeedData();

        given()
                .when()
                .header(getProfessorAuthHeader(seedData.professorId()))
                .queryParam("courseId", seedData.courseId())
                .body(new CreateLectureRequest(seedData.courseId(), 5, List.of(
                        new TimeSlot(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0), LocalTime.of(11, 30))
                )))
                .contentType(ContentType.JSON)
                .post("/lectures/create")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Creating a lecture from a non-existent course should return 404")
    void createLectureFromCourse_shouldReturn404_ifCourseDoesNotExist() {
        var seedData = createCourseSeedData();

        given()
                .when()
                .header(getProfessorAuthHeader(seedData.professorId()))
                .queryParam("courseId", 999L)
                .body(new CreateLectureRequest(999L, 5, List.of(
                        new TimeSlot(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0), LocalTime.of(11, 30))
                )))
                .contentType(ContentType.JSON)
                .post("/lectures/create")
                .then()
                .statusCode(404);
    }

    public record LectureSeedData(Long lectureId, Long studentId, Long professorId) {
    }

    /**
     * <ul>
     *     <li>Must create a professor.</li>
     *     <li>The professor must create a lecture that is open for enrollment.</li>
     *     <li>The lecture must have space for 1 student</li>
     *     <li>Must create a student that can enroll to the lecture.</li>
     * </ul>
     */
    protected abstract LectureSeedData createLectureSeedData();

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
                .statusCode(201);

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
                .statusCode(200);
    }

    @Test
    @DisplayName("Getting lecture details for a non-existent lecture should return 404")
    void getLectureDetails_shouldReturn404_ifLectureDoesNotExist() {
        given()
                .when()
                .get("/lectures/{lectureId}", 1L)
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

    public record SecondProfessorSeedData(Long professorId) {
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

    public record AssignGradeSeedData(Long assessmentId) {
    }

    /**
     * Must create an assessment from the assessmentTimeSlot.
     */
    protected abstract AssignGradeSeedData createAssignGradeSeedData(LectureSeedData lectureSeedData, TimeSlot assessmentTimeSlot);

    @Test
    @DisplayName("Assigning a grade to an enrolled student should return 201 and updating it should return 200")
    void assignGrade_shouldReturn201_ifStudentIsEnrolled() {
        // System date is 02.12.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // 01.12.2025 -> it's allowed to assign a grade
        TimeSlot assessmentTimeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        var lectureSeedData = createLectureSeedData();
        var assignGradeSeedData = createAssignGradeSeedData(lectureSeedData, assessmentTimeSlot);

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
                .statusCode(400);

        // let's enroll the student
        given()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.studentId())
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
        var updateRequest = new AssignGradeRequest(lectureSeedData.studentId(), lectureSeedData.studentId(), 100);
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
    @DisplayName("Updating a grade that does not exist should return 404")
    void updateGrade_shouldReturn404_ifNoGradeExists() {
        var lectureSeedData = createLectureSeedData();

        var request = new AssignGradeRequest(1L, 1L, 95);

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
        // System date is 02.12.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // 01.12.2025 -> it's allowed to assign a grade
        TimeSlot assessmentTimeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        var lectureSeedData = createLectureSeedData();
        var assignGradeSeedData = createAssignGradeSeedData(lectureSeedData, assessmentTimeSlot);
        var professor2Id = createSecondProfessorSeedData().professorId();

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

        var request = new AssignGradeRequest(lectureSeedData.studentId(), 999L, 90);

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
                .header(getProfessorAuthHeader(lectureSeedData.professorId()))
                .post("/lectures/{lectureId}", lectureSeedData.lectureId())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Updating a grade by a professor that does not own the lecture should return 403")
    void updateGrade_shouldReturn403_ifNotOwnedByProfessor() {
        // System date is 02.12.2025
        setSystemTime(Clock.fixed(
                LocalDateTime.of(2025, 12, 2, 10, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")));

        // 01.12.2025 -> it's allowed to assign a grade
        TimeSlot assessmentTimeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        var lectureSeedData = createLectureSeedData();
        var assignGradeSeedData = createAssignGradeSeedData(lectureSeedData, assessmentTimeSlot);
        var professor2Id = createCourseSeedData().professorId();

        var request = new AssignGradeRequest(lectureSeedData.studentId(), assignGradeSeedData.assessmentId(), 90);

        // enroll the student
        given()
                .header(getStudentAuthHeader(lectureSeedData.studentId()))
                .post("/lectures/{lectureId}/enroll", lectureSeedData.lectureId())
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

    public record WaitingListSeedData(Long student2Id) {
    }

    /**
     * Must create a second student.
     */
    protected abstract WaitingListSeedData createWaitingListSeedData();

    @Test
    @DisplayName("Enrolling a student when a lecture is full should add them to the waiting list and getting the waiting list should return 200")
    void getWaitingListForLecture_shouldReturn200() {
        var lectureSeedData = createLectureSeedData();
        var waitingListSeedData = createWaitingListSeedData();

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
                .header(getStudentAuthHeader(waitingListSeedData.student2Id()))
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
                .queryParam("studentId", waitingListSeedData.student2Id())
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
                .get("/lectures/{lectureId}/waitingList", 999L)
                .then()
                .statusCode(404);
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
