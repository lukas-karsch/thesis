package karsch.lukas.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Configuration
public class DatabaseMetricsConfig {

    @Bean
    public MeterBinder databaseSizeBinder(DataSource dataSource) {
        return registry -> Gauge.builder("database.size.bytes", dataSource, ds -> {
            log.debug("Executing databaseSizeBinder");
            try (Connection conn = ds.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT pg_database_size(current_database())")) {
                if (rs.next()) return rs.getLong(1);
            } catch (SQLException e) {
                return 0.0;
            }
            return 0.0;
        }).register(registry);
    }
}
