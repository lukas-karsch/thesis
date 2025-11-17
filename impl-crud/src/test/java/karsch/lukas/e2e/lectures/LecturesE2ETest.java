package karsch.lukas.e2e.lectures;

import karsch.lukas.PostgresTestcontainerConfiguration;
import karsch.lukas.lectures.AbstractLecturesE2ETest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(PostgresTestcontainerConfiguration.class)
public class LecturesE2ETest extends AbstractLecturesE2ETest {

    private static final Logger log = LoggerFactory.getLogger(LecturesE2ETest.class);

    @LocalServerPort
    private Integer port;

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
}
