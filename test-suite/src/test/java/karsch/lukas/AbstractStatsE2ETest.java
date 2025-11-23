package karsch.lukas;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractStatsE2ETest implements BaseE2ETest {

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + getPort();
    }

    @AfterEach
    void tearDown() {
        resetDatabase();
    }

    public record GradingSeedData(long studentId) {
    }

    /**
     * <ol>
     *     <li>Must create a student</li>
     *     <li>Must create a professor</li>
     *     <li>Must create two courses, each worth 5 credits</li>
     *     <li>Must create a lecture for each course</li>
     *     <li>Must create an assessment for each lecture</li>
     *     <li>Must assign grades to the student - one failed, one passed</li>
     * </ol>
     */
    protected abstract GradingSeedData createGradingSeedData();

    @Test
    @DisplayName("A student should see his accumulated credits")
    void getAccumulatedCredits() {
        var gradingSeedData = createGradingSeedData();

        given()
                .queryParam("studentId", gradingSeedData.studentId())
                .get("/stats/credits")
                .then()
                .body("data.totalCredits", equalTo(5));
    }
}
