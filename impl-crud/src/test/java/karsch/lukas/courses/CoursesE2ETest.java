package karsch.lukas.courses;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CoursesE2ETest extends AbstractCoursesE2ETest {

    @LocalServerPort
    private Integer port;

    @Override
    public int getPort() {
        return port;
    }
}
