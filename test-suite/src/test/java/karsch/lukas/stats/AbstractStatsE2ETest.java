package karsch.lukas.stats;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import karsch.lukas.BaseE2ETest;
import karsch.lukas.lecture.AssignGradeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

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
        setSystemTime(Clock.systemUTC());
    }

    public record GradingSeedData(UUID studentId, UUID passedLectureId,
                                  UUID passedLectureAssessmentId, UUID professorId) {
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
        setSystemTime(Clock.fixed(LocalDateTime.of(2025, 11, 24, 12, 0, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

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
        setSystemTime(Clock.fixed(LocalDateTime.of(2025, 11, 24, 12, 0, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

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
        setSystemTime(Clock.fixed(LocalDateTime.of(2025, 11, 24, 12, 0, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

        var gradingSeedData = createGradingSeedData();

        setSystemTime(Clock.fixed(LocalDateTime.of(2025, 11, 24, 13, 0, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

        given()
                .header(getProfessorAuthHeader(gradingSeedData.professorId()))
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

    @Test
    @DisplayName("Should return the correct grade history for a lecture assessment with date filter")
    void getGradesHistoryWithDateFilter() {
        // Arrange
        var T0 = LocalDateTime.of(2025, 11, 24, 12, 0, 0);
        var T1 = LocalDateTime.of(2025, 11, 24, 13, 0, 0);
        var T2 = LocalDateTime.of(2025, 11, 24, 14, 0, 0);

        setSystemTime(Clock.fixed(T0.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        var gradingSeedData = createGradingSeedData(); // initial grade 100 at T0

        setSystemTime(Clock.fixed(T1.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        given()
                .header(getProfessorAuthHeader(gradingSeedData.professorId()))
                .contentType(ContentType.JSON)
                .body(new AssignGradeRequest(
                        gradingSeedData.studentId(),
                        gradingSeedData.passedLectureAssessmentId(),
                        80
                ))
                .patch("/lectures/{lectureId}", gradingSeedData.passedLectureId())
                .then()
                .statusCode(200);

        setSystemTime(Clock.fixed(T2.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        given()
                .header(getProfessorAuthHeader(gradingSeedData.professorId()))
                .contentType(ContentType.JSON)
                .body(new AssignGradeRequest(
                        gradingSeedData.studentId(),
                        gradingSeedData.passedLectureAssessmentId(),
                        70
                ))
                .patch("/lectures/{lectureId}", gradingSeedData.passedLectureId())
                .then()
                .statusCode(200);

        given()
                .queryParam("studentId", gradingSeedData.studentId())
                .queryParam("lectureAssessmentId", gradingSeedData.passedLectureAssessmentId())
                .queryParam("startDate", T1.toString())
                .queryParam("endDate", T2.toString())
                .get("/stats/grades/history")
                .then()
                .statusCode(200)
                .body("data.history", hasSize(1))
                .body("data.history[0].grade", equalTo(80));
    }
}
