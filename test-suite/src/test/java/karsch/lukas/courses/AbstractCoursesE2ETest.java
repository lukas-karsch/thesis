package karsch.lukas.courses;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import karsch.lukas.BaseE2ETest;
import karsch.lukas.course.CreateCourseRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static karsch.lukas.helper.AuthHelper.getProfessorAuthHeader;
import static karsch.lukas.helper.AuthHelper.getStudentAuthHeader;
import static org.hamcrest.Matchers.*;

public abstract class AbstractCoursesE2ETest implements BaseE2ETest {

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + getPort();
    }

    @AfterEach
    void tearDown() {
        resetDatabase();
    }

    @Test
    @DisplayName("GET /courses should return status code 200")
    void getCourses_shouldReturn200() {
        given()
                .when()
                .get("/courses")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /courses should 403 if not authenticated as professor")
    void createCourses_shouldReturn403_whenNotAuthenticatedAsProfessor() {
        var request = new CreateCourseRequest(
                "Maths",
                "Simple mathematics",
                5,
                Collections.emptySet(),
                0
        );

        given()
                .body(request)
                .header(getStudentAuthHeader(UUID.randomUUID()))
                .contentType(ContentType.JSON)
                .when()
                .post("/courses")
                .then()
                .statusCode(403)
                .body("status", equalTo("error"));
    }

    @Test
    @DisplayName("POST /courses should return status code 201; GET /courses should return a list")
    void createCourses_shouldReturn201() {
        int minimumCreditsRequired = 1;

        var request = new CreateCourseRequest(
                "Maths",
                "Simple mathematics",
                5,
                Collections.emptySet(),
                minimumCreditsRequired
        );

        given()
                .body(request)
                .header(getProfessorAuthHeader(UUID.randomUUID()))
                .contentType(ContentType.JSON)
                .when()
                .post("/courses")
                .then()
                .statusCode(201)
                .body("status", equalTo("success"));

        given()
                .when()
                .get("/courses")
                .then()
                .statusCode(200)
                .body("data", hasSize(1))
                .body("data[0].name", equalTo("Maths"))
                .body("data[0].minimumCreditsRequired", equalTo(minimumCreditsRequired));
    }

    @Test
    @DisplayName("POST /courses should 404 when prerequisite course does not exist")
    void createCourses_should404_whenPrerequisiteCourseDoesNotExist() {
        var prerequisiteId = UUID.randomUUID();

        var request = new CreateCourseRequest(
                "Advanced Maths",
                "Only for nerds",
                5,
                Set.of(prerequisiteId),
                0
        );

        given()
                .body(request)
                .header(getProfessorAuthHeader(UUID.randomUUID()))
                .contentType(ContentType.JSON)
                .when()
                .post("/courses")
                .then()
                .statusCode(404)
                .body("status", equalTo("error"));
    }

    @Test
    @DisplayName("POST /courses with prerequisites")
    void createCourses_withPrerequisites() {
        var baseRequest = new CreateCourseRequest(
                "Maths",
                "Basic",
                5,
                Collections.emptySet(),
                0
        );

        var createdUuid = given()
                .body(baseRequest)
                .header(getProfessorAuthHeader(UUID.randomUUID()))
                .contentType(ContentType.JSON)
                .when()
                .post("/courses")
                .then()
                .statusCode(201)
                .body("status", equalTo("success"))
                .extract()
                .body()
                .jsonPath().getString("data");

        var advancedRequest = new CreateCourseRequest(
                "Advanced Maths",
                "Only for nerds",
                5,
                Set.of(UUID.fromString(createdUuid)),
                0
        );

        given()
                .body(advancedRequest)
                .header(getProfessorAuthHeader(UUID.randomUUID()))
                .contentType(ContentType.JSON)
                .when()
                .post("/courses")
                .then()
                .statusCode(201)
                .body("status", equalTo("success"));

        given()
                .when()
                .get("/courses")
                .then()
                .statusCode(200)
                .body("data.name", hasItem("Maths"))
                .body("data.find { it.name == 'Advanced Maths' }.prerequisites.name", hasItem("Maths"));
    }
}
