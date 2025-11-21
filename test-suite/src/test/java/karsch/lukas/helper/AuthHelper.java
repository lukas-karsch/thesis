package karsch.lukas.helper;

import io.restassured.http.Header;

public class AuthHelper {
    public static Header getStudentAuthHeader(Long studentId) {
        return new Header("customAuth", String.format("student_%d", studentId));
    }

    public static Header getProfessorAuthHeader(Long professorId) {
        return new Header("customAuth", String.format("professor_%d", professorId));
    }
}
