package karsch.lukas.core.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateTimeProviderConfig {

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return DateTimeProvider.getInstance();
    }
}
