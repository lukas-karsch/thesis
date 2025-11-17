package karsch.lukas.lectures;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import karsch.lukas.BaseE2ETest;
import karsch.lukas.lecture.*;
import karsch.lukas.stats.AssessmentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static io.restassured.RestAssured.given;

public abstract class AbstractLecturesE2ETest implements BaseE2ETest {

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + getPort();
    }

    @AfterEach
    void tearDown() {
        resetDatabase();
    }

    @Test
    @DisplayName("GET /lectures should return status code 200")
    void getLecturesForStudent_shouldReturn200() {
        given()
                .when()
                .header(new Header("customAuth", "student_1"))
                .queryParam("studentId", 1L)
                .get("/lectures")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /lectures/{lectureId}/enroll should return status code 201; DELETE the enrollment should be 200")
    void enrollToLecture_shouldReturn201() {
        given()
                .when()
                .header(new Header("customAuth", "student_1"))
                .post("/lectures/1/enroll")
                .then()
                .statusCode(201);

        // cancel the enrollment
        given()
                .when()
                .header(new Header("customAuth", "student_1"))
                .delete("/lectures/1/enroll")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /lectures/create should return status code 201")
    void createLectureFromCourse_shouldReturn201() {
        given()
                .when()
                .header(new Header("customAuth", "professor_1"))
                .queryParam("courseId", 1L)
                .post("/lectures/create")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("GET /lectures/{lectureId} should return status code 200")
    void getLectureDetails_shouldReturn200() {
        given()
                .when()
                .get("/lectures/1")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /lectures/{lectureId} should return status code 201")
    void assignGrade_shouldReturn201() {
        var request = new AssignGradeRequest(1L, AssessmentType.EXAM, 90);
        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(new Header("customAuth", "professor_1"))
                .post("/lectures/1")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("PATCH /lectures/{lectureId} should return status code 200")
    void updateGrade_shouldReturn200() {
        var request = new AssignGradeRequest(1L, AssessmentType.EXAM, 95);

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(new Header("customAuth", "professor_1"))
                .patch("/lectures/1")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /lectures/{lectureId}/dates should return status code 201")
    void addDatesToLecture_shouldReturn201() {
        var request = new AssignDatesToLectureRequest(
                Collections.singleton(new TimeSlot(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0)))
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(new Header("customAuth", "professor_1"))
                .post("/lectures/1/dates")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("POST /lectures/{lectureId}/assessments should return status code 201")
    void addAssessmentForLecture_shouldReturn201() {
        var request = new LectureAssessmentDTO(
                AssessmentType.EXAM, new TimeSlot(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0))
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .header(new Header("customAuth", "professor_1"))
                .post("/lectures/1/assessments")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("GET /lectures/{lectureId}/waitingList should return status code 200")
    void getWaitingListForLecture_shouldReturn200() {
        given()
                .when()
                .get("/lectures/1/waitingList")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /lectures/{lectureId}/lifecycle should return status code 201")
    void advanceLifecycleOfLecture_shouldReturn201() {
        given()
                .when()
                .header(new Header("customAuth", "professor_1"))
                .queryParam("newLectureStatus", LectureStatus.IN_PROGRESS)
                .post("/lectures/1/lifecycle")
                .then()
                .statusCode(201);
    }
}
