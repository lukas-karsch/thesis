package karsch.lukas.audit;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.course.CreateCourseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static karsch.lukas.helper.AuthHelper.getProfessorAuthHeader;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(PostgresTestcontainerConfiguration.class)
public class AuditLogTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    void creatingAnEntity_shouldAddToAuditLog() {
        given()
                .contentType(ContentType.JSON)
                .header(getProfessorAuthHeader(UUID.randomUUID()))
                .body(new CreateCourseRequest(
                        "Computer Science",
                        null,
                        5,
                        Set.of(),
                        0
                ))
                .post("/courses")
                .then()
                .statusCode(201);

        assertThat(auditLogRepository.findAll()).hasSize(1).first().satisfies(
                a -> assertThat(a.getOperation()).isEqualTo("CREATE")
        );
    }
}
