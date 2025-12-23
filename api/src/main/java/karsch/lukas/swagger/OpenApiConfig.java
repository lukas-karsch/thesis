package karsch.lukas.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
        name = "studentAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "customAuth",
        description = "Authentication token in the format 'student_[UUID]'"
)
@SecurityScheme(
        name = "professorAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "customAuth",
        description = "Authentication token in the format 'professor_[UUID]'"
)
@Configuration
public class OpenApiConfig {
    public static final String PROFESSOR_AUTH = "professorAuth";
    public static final String STUDENT_AUTH = "studentAuth";
}
