package karsch.lukas.stats;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import karsch.lukas.BaseE2ETest;
import karsch.lukas.lecture.AssignGradeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static karsch.lukas.helper.AuthHelper.getProfessorAuthHeader;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public abstract class AbstractStatsE2ETest implements BaseE2ETest {

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + getPort();
    }

    @AfterEach
    void tearDown() {
        resetDatabase();
    }

    public record GradingSeedData(long studentId, long passedLectureId,
                                  long passedLectureAssessmentId) {
    }

    /**
     * <ol>
     *     <li>Must create a student</li>
     *     <li>Must create a professor</li>
     *     <li>Must create two courses, each worth 5 credits</li>
     *     <li>Must create a lecture for each course</li>
     *     <li>Must create an assessment for each lecture with weight 1</li>
     *     <li>Must assign grades to the student - one failed (0), one passed (100)</li>
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

    @Test
    @DisplayName("Should correctly return the student's grades")
    void getGradesForStudent() {
        var gradingSeedData = createGradingSeedData();

        given()
                .queryParam("studentId", gradingSeedData.studentId())
                .get("/stats/grades")
                .then()
                .statusCode(200)
                .body("data.grades", hasSize(2))
                .body("data.grades.find { it.combinedGrade == 0 }.credits", equalTo(0))
                .body("data.grades.find { it.combinedGrade == 0 }.isFinalGrade", equalTo(true))
                .body("data.grades.find { it.combinedGrade == 100 }.credits", equalTo(5))
                .body("data.grades.find { it.combinedGrade == 100 }.isFinalGrade", equalTo(true));
    }

    @Test
    @DisplayName("Should return the correct grade history for a lecture assessment")
    void getGradesHistory() {
        var gradingSeedData = createGradingSeedData();

        given()
                .header(getProfessorAuthHeader(1L))
                .contentType(ContentType.JSON)
                .body(new AssignGradeRequest(
                        gradingSeedData.studentId(),
                        gradingSeedData.passedLectureAssessmentId(),
                        80
                ))
                .patch("/lectures/{lectureId}", gradingSeedData.passedLectureId())
                .then()
                .statusCode(200);

        given()
                .queryParam("studentId", gradingSeedData.studentId())
                .queryParam("lectureAssessmentId", gradingSeedData.passedLectureAssessmentId())
                .get("/stats/grades/history")
                .then()
                .statusCode(200)
                .body("data.history", hasSize(2))
                .body("data.history[0].grade", equalTo(80))
                .body("data.history[1].grade", equalTo(100));
    }
}
