package karsch.lukas.e2e.courses;

import karsch.lukas.AxonTestcontainerConfiguration;
import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.courses.AbstractCoursesE2ETest;
import karsch.lukas.e2e.config.AxonTestConfiguration;
import karsch.lukas.time.DateTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import({PostgresTestcontainerConfiguration.class, AxonTestcontainerConfiguration.class, AxonTestConfiguration.class})
@Slf4j
public class EsCqrsCoursesE2ETest extends AbstractCoursesE2ETest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private DateTimeProvider dateTimeProvider;

    @Autowired
    private DataSource dataSource;

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void resetDatabase() {
        try (
                Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()
        ) {
            log.info("Resetting database...");
            var rs = conn.getMetaData().getTables(null, "public", "%", new String[]{"TABLE"});
            while (rs.next()) {
                String table = rs.getString("TABLE_NAME");
                stmt.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
            }
        } catch (SQLException e) {
            log.error("Could not reset database", e);
        }
    }

    @Override
    public void setSystemTime(Clock clock) {
        dateTimeProvider.setClock(clock);
    }
}
