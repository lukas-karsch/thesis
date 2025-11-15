package karsch.lukas.courses;

import io.restassured.RestAssured;
import karsch.lukas.BaseE2ETest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

public abstract class AbstractCoursesE2ETest implements BaseE2ETest {

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + getPort();
    }

    @Test
    @DisplayName("/courses should return status code 200")
    void getCourses_shouldReturn200_emptyList() {
        given()
                .when()
                .get("/courses")
                .then()
                .statusCode(200)
                .body("data", hasSize(0));
    }
}
