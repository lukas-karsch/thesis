package karsch.lukas.helper;

import io.restassured.http.Header;

import java.util.UUID;

public class AuthHelper {
    public static Header getStudentAuthHeader(UUID studentId) {
        return new Header("customAuth", String.format("student_%s", studentId));
    }

    public static Header getProfessorAuthHeader(UUID professorId) {
        return new Header("customAuth", String.format("professor_%s", professorId));
    }
}
